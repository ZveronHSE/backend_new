package ru.zveron.service.api

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import ru.zveron.contract.profile.model.ChannelType
import ru.zveron.contract.profile.LotStatus
import ru.zveron.ProfileTest
import ru.zveron.mapper.AddressMapper.toProfileAddress
import ru.zveron.commons.assertions.addressShouldBe
import ru.zveron.commons.assertions.channelsShouldBe
import ru.zveron.commons.assertions.linksShouldBe
import ru.zveron.commons.assertions.lotShouldBe
import ru.zveron.commons.assertions.profileShouldBe
import ru.zveron.commons.assertions.responseShouldBe
import ru.zveron.commons.assertions.responseShouldBeBlockedAnd
import ru.zveron.commons.generator.AddressGenerator.generateAddress
import ru.zveron.commons.generator.CommunicationLinksGenerator
import ru.zveron.commons.generator.LotsGenerator
import ru.zveron.commons.generator.ProfileGenerator
import ru.zveron.commons.generator.PropsGenerator
import ru.zveron.commons.generator.SettingsGenerator
import ru.zveron.contract.addressResponse
import ru.zveron.contract.lot.profileLotsResponse
import ru.zveron.contract.profile.deleteProfileRequest
import ru.zveron.exception.ProfileException
import ru.zveron.exception.ProfileNotFoundException
import ru.zveron.contract.profile.getChannelTypesRequest
import ru.zveron.contract.profile.getLinksRequest
import ru.zveron.contract.profile.getProfileInfoRequest
import ru.zveron.contract.profile.getProfilePageRequest
import ru.zveron.contract.profile.getSettingsRequest
import ru.zveron.repository.ProfileRepository
import ru.zveron.repository.SettingsRepository
import ru.zveron.service.client.address.AddressClient
import ru.zveron.service.client.blakclist.BlacklistClient
import ru.zveron.service.client.lot.LotClient
import ru.zveron.service.client.review.ReviewClient
import ru.zveron.contract.profile.setProfileInfoRequest
import ru.zveron.contract.profile.setSettingsRequest
import ru.zveron.mapper.AddressMapper.toRequest
import ru.zveron.repository.CommunicationLinkRepository
import java.time.Instant

class ProfileServiceExternalTest : ProfileTest() {

    @Autowired
    lateinit var service: ProfileServiceExternal

    @Autowired
    lateinit var profileRepository: ProfileRepository

    @Autowired
    lateinit var settingsRepository: SettingsRepository

    @Autowired
    lateinit var communicationLinkRepository: CommunicationLinkRepository

    @TestConfiguration
    class ProfileServiceExternalTestConfiguration {
        @Bean
        fun addressClient() = mockk<AddressClient>()

        @Bean
        fun blacklistClient() = mockk<BlacklistClient>()

        @Bean
        fun lotClient() = mockk<LotClient>()

        @Bean
        fun reviewClient() = mockk<ReviewClient>()
    }

    @Autowired
    lateinit var addressClient: AddressClient

    @Autowired
    lateinit var blacklistClient: BlacklistClient

    @Autowired
    lateinit var lotClient: LotClient

    @Autowired
    lateinit var reviewClient: ReviewClient

    @ParameterizedTest
    @ValueSource(longs = [0, 10])
    fun `getProfilePage when id request is correct`(authorizedId: Long) {
        val now = Instant.now()
        val (id, addressId) = PropsGenerator.generateNIds(2)
        val expectedProfile = ProfileGenerator.generateProfile(id, now, addressId)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        CommunicationLinksGenerator.generateLinks(expectedProfile, addPhone = true)
        profileRepository.save(expectedProfile)
        val request = getProfilePageRequest {
            requestedProfileId = id
            authorizedProfileId = authorizedId
        }
        coEvery { blacklistClient.existsInBlacklist(id, authorizedId) } returns false
        val activeLot = LotsGenerator.generateLot(false)
        val closedLot = LotsGenerator.generateLot(false)
        coEvery { lotClient.getLotsBySellerId(id) } returns profileLotsResponse {
            activateLots.add(activeLot)
            inactivateLots.add(closedLot)
        }
        val address = generateAddress(addressId)
        coEvery { addressClient.getById(addressId) } returns address
        val rating = PropsGenerator.generateDouble()
        coEvery { reviewClient.getRating(id) } returns rating

        runBlocking {
            val response = service.getProfilePage(request)

            response responseShouldBe expectedProfile
            response.address shouldBe address.toProfileAddress()
            response.rating shouldBe rating
            //TODO: response.reviewNumber should be (?)
            response.activeLotsList.apply {
                size shouldBe 1
                first() lotShouldBe activeLot
                first().status shouldBe LotStatus.ACTIVE
            }
            response.closedLotsList.apply {
                size shouldBe 1
                first() lotShouldBe closedLot
                first().status shouldBe LotStatus.CLOSED
            }
        }
    }

    @Test
    fun `getProfilePage when authorized and in blacklist`() {
        val now = Instant.now()
        val (id, authorizedProfile, addressId) = PropsGenerator.generateNIds(3)
        val expectedProfile = ProfileGenerator.generateProfile(id, now, addressId)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        CommunicationLinksGenerator.generateLinks(expectedProfile, addPhone = true)
        profileRepository.save(expectedProfile)
        val request = getProfilePageRequest {
            requestedProfileId = id
            authorizedProfileId = authorizedProfile
        }
        coEvery { blacklistClient.existsInBlacklist(id, authorizedProfile) } returns true

        runBlocking {
            val response = service.getProfilePage(request)

            response responseShouldBeBlockedAnd expectedProfile
            response.address shouldBe ""
            response.rating shouldBe 0.0
            //TODO: response.reviewNumber should be (?)
            response.activeLotsList.size shouldBe 0
            response.closedLotsList.size shouldBe 0
        }
    }

    @Test
    fun `getProfilePage when id is wrong`() {
        val id = PropsGenerator.generateUserId()
        val request = getProfilePageRequest { requestedProfileId = id }

        val exception = shouldThrow<ProfileNotFoundException> {
            runBlocking {
                service.getProfilePage(request)
            }
        }
        exception.message shouldBe "Profile with id: $id doesn't exist"
    }

    @Test
    fun `getProfileInfo when request is correct`() {
        val now = Instant.now()
        val (id, addressId) = PropsGenerator.generateNIds(2)
        val expectedProfile = ProfileGenerator.generateProfile(id, now, addressId)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        CommunicationLinksGenerator.generateLinks(expectedProfile, addPhone = true)
        profileRepository.save(expectedProfile)
        val request = getProfileInfoRequest { this.id = id }
        val address = generateAddress(addressId)
        coEvery { addressClient.getById(addressId) } returns address
        val rating = PropsGenerator.generateDouble()
        coEvery { reviewClient.getRating(id) } returns rating

        runBlocking {
            val response = service.getProfileInfo(request)

            response responseShouldBe expectedProfile
            response.address addressShouldBe address
            response.rating shouldBe rating
        }
    }

    @Test
    fun `getProfileInfo when id is incorrect`() {
        val id = PropsGenerator.generateUserId()
        val request = getProfileInfoRequest { this.id = id }

        val exception = shouldThrow<ProfileNotFoundException> {
            runBlocking {
                service.getProfileInfo(request)
            }
        }
        exception.message shouldBe "Profile with id: $id doesn't exist"
    }

    @Test
    fun `setProfileInfo when request is correct`() {
        val now = Instant.now()
        val (id, addressId) = PropsGenerator.generateNIds(2)
        val expectedProfile = ProfileGenerator.generateProfile(id, now, addressId)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        CommunicationLinksGenerator.generateLinks(expectedProfile, addPhone = true)
        profileRepository.save(expectedProfile)

        val request = setProfileInfoRequest {
            this.id = id
            name = PropsGenerator.generateString(10)
            surname = PropsGenerator.generateString(10)
            imageId = PropsGenerator.generateUserId()
            address = generateAddress()
        }
        val newAddressId = PropsGenerator.generateUserId()
        coEvery { addressClient.saveIfNotExists(request.address.toRequest()) } returns addressResponse {
            this.id = newAddressId
        }

        runBlocking {
            service.setProfileInfo(request)
        }

        val actualProfile = profileRepository.findById(id).get()
        actualProfile profileShouldBe request
        actualProfile.addressId shouldBe newAddressId
    }

    @Test
    fun `setProfileInfo when id is incorrect`() {
        val id = PropsGenerator.generateUserId()
        val request = setProfileInfoRequest { this.id = id }

        val exception = shouldThrow<ProfileNotFoundException> {
            runBlocking {
                service.setProfileInfo(request)
            }
        }
        exception.message shouldBe "Profile with id: $id doesn't exist"
    }

    @Test
    fun `getChannelTypes when request is correct`() {
        val now = Instant.now()
        val (id, addressId) = PropsGenerator.generateNIds(2)
        val expectedProfile = ProfileGenerator.generateProfile(id, now, addressId)
        val settings = SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        CommunicationLinksGenerator.generateLinks(expectedProfile, addPhone = true)
        profileRepository.save(expectedProfile)
        val request = getChannelTypesRequest { this.id = id }

        runBlocking {
            val response = service.getChannelTypes(request)

            response.channelsList channelsShouldBe settings.channels
        }
    }

    @Test
    fun `getChannelTypes when id is incorrect`() {
        val id = PropsGenerator.generateUserId()
        val request = getChannelTypesRequest { this.id = id }

        val exception = shouldThrow<ProfileNotFoundException> {
            runBlocking {
                service.getChannelTypes(request)
            }
        }
        exception.message shouldBe "Profile with id: $id doesn't exist"
    }

    @Test
    fun `getLinks when request is correct`() {
        val now = Instant.now()
        val (id, addressId) = PropsGenerator.generateNIds(2)
        val expectedProfile = ProfileGenerator.generateProfile(id, now, addressId)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        val contacts = CommunicationLinksGenerator.generateLinks(expectedProfile, addPhone = true)
        profileRepository.save(expectedProfile)
        val request = getLinksRequest { this.id = id }

        runBlocking {
            val links = service.getLinks(request)

            links linksShouldBe contacts
        }
    }

    @Test
    fun `getLinks when id is incorrect`() {
        val id = PropsGenerator.generateUserId()
        val request = getLinksRequest { this.id = id }

        val exception = shouldThrow<ProfileNotFoundException> {
            runBlocking {
                service.getLinks(request)
            }
        }
        exception.message shouldBe "Profile with id: $id doesn't exist"
    }

    @Test
    fun `getSettings when request is correct`() {
        val now = Instant.now()
        val (id, addressId) = PropsGenerator.generateNIds(2)
        val expectedProfile = ProfileGenerator.generateProfile(id, now)
        val settings =
            SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true, addressId = addressId)
        CommunicationLinksGenerator.generateLinks(expectedProfile, addPhone = true)
        profileRepository.save(expectedProfile)
        val request = getSettingsRequest { this.id = id }
        val address = generateAddress(addressId)
        coEvery { addressClient.getById(addressId) } returns address

        runBlocking {
            val response = service.getSettings(request)

            response.channelsList channelsShouldBe settings.channels
            response.address addressShouldBe address
        }
    }

    @Test
    fun `getSettings when id is incorrect`() {
        val id = PropsGenerator.generateUserId()
        val request = getLinksRequest { this.id = id }

        val exception = shouldThrow<ProfileNotFoundException> {
            runBlocking {
                service.getLinks(request)
            }
        }
        exception.message shouldBe "Profile with id: $id doesn't exist"
    }

    @Test
    fun `setSettings when request is correct`() {
        val now = Instant.now()
        val (id, addressId) = PropsGenerator.generateNIds(2)
        val expectedProfile = ProfileGenerator.generateProfile(id, now)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true, addressId = addressId)
        CommunicationLinksGenerator.generateLinks(expectedProfile, addPhone = true, addVk = true, addGmail = true)
        profileRepository.save(expectedProfile)
        val request = setSettingsRequest {
            this.id = id
            address = generateAddress()
            channels.addAll(listOf(ChannelType.VK, ChannelType.GOOGLE))
        }
        coEvery { addressClient.saveIfNotExists(request.address.toRequest()) } returns addressResponse {
            this.id = addressId
        }

        runBlocking {
            service.setSettings(request)

            val actualSettings = settingsRepository.findById(id).get()
            actualSettings.channels channelsShouldBe request.channelsList
            actualSettings.searchAddressId shouldBe addressId
        }
    }

    @Test
    fun `setSettings when channels is missed`() {
        val now = Instant.now()
        val (id, addressId) = PropsGenerator.generateNIds(2)
        val expectedProfile = ProfileGenerator.generateProfile(id, now)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true, addressId = addressId)
        CommunicationLinksGenerator.generateLinks(expectedProfile, addPhone = true, addVk = true)
        profileRepository.save(expectedProfile)
        val request = setSettingsRequest {
            this.id = id
            address = generateAddress()
            channels.addAll(listOf(ChannelType.VK, ChannelType.GOOGLE))
        }

        val exception = shouldThrow<ProfileException> {
            runBlocking {
                service.setSettings(request)
            }
        }
        exception.message shouldBe "Can't use gmail as communication channel because link is missed"
    }

    @Test
    fun `setSettings when wrong number of channels`() {
        val now = Instant.now()
        val (id, addressId) = PropsGenerator.generateNIds(2)
        val expectedProfile = ProfileGenerator.generateProfile(id, now)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true, addressId = addressId)
        CommunicationLinksGenerator.generateLinks(expectedProfile, addPhone = true, addVk = true)
        profileRepository.save(expectedProfile)
        val request = setSettingsRequest {
            this.id = id
            address = generateAddress()
        }

        val exception = shouldThrow<ProfileException> {
            runBlocking {
                service.setSettings(request)
            }
        }
        exception.message shouldBe "Invalid number of communication ways. Expected 1 or 2, but was: 0"
    }

    @Test
    fun `setSettings when id is incorrect`() {
        val id = PropsGenerator.generateUserId()
        val request = setSettingsRequest { this.id = id }

        val exception = shouldThrow<ProfileNotFoundException> {
            runBlocking {
                service.setSettings(request)
            }
        }
        exception.message shouldBe "Profile with id: $id doesn't exist"
    }

    @Test
    fun `deleteProfile when request is correct`() {
        val now = Instant.now()
        val (id, addressId) = PropsGenerator.generateNIds(2)
        val expectedProfile = ProfileGenerator.generateProfile(id, now)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true, addressId = addressId)
        CommunicationLinksGenerator.generateLinks(expectedProfile, addPhone = true, addVk = true)
        profileRepository.save(expectedProfile)
        val request = deleteProfileRequest { this.id = id }

        runBlocking {
            service.deleteProfile(request)
        }

        profileRepository.findById(id).isPresent shouldBe false
        settingsRepository.findById(id).isPresent shouldBe false
        communicationLinkRepository.findAllByProfileId(id).size shouldBe 0

        // TODO: service should write to kafka
    }
}