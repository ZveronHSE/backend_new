package ru.zveron.service.profile

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
import ru.zveron.ChannelType
import ru.zveron.LotStatus
import ru.zveron.ProfileTest
import ru.zveron.mapper.AddressMapper
import ru.zveron.mapper.AddressMapper.toProfileAddress
import ru.zveron.commons.assertions.addressShouldBe
import ru.zveron.commons.assertions.channelsShouldBe
import ru.zveron.commons.assertions.linksShouldBe
import ru.zveron.commons.assertions.lotShouldBe
import ru.zveron.commons.assertions.profileShouldBe
import ru.zveron.commons.assertions.responseShouldBe
import ru.zveron.commons.assertions.responseShouldBeBlockedAnd
import ru.zveron.commons.generator.AddressGenerator
import ru.zveron.commons.generator.AddressGenerator.generateAddress
import ru.zveron.commons.generator.ContactsGenerator
import ru.zveron.commons.generator.LotsGenerator
import ru.zveron.commons.generator.ProfileGenerator
import ru.zveron.commons.generator.PropsGenerator
import ru.zveron.commons.generator.SettingsGenerator
import ru.zveron.contract.addressResponse
import ru.zveron.contract.lot.profileLotsResponse
import ru.zveron.deleteProfileRequest
import ru.zveron.exception.ProfileException
import ru.zveron.getChannelTypesRequest
import ru.zveron.getLinksRequest
import ru.zveron.getProfileInfoRequest
import ru.zveron.getProfilePageRequest
import ru.zveron.getSettingsRequest
import ru.zveron.repository.ContactRepository
import ru.zveron.repository.ProfileRepository
import ru.zveron.repository.SettingsRepository
import ru.zveron.service.api.address.AddressService
import ru.zveron.service.api.blakclist.BlacklistService
import ru.zveron.service.api.lot.LotService
import ru.zveron.service.api.profile.ProfileServiceExternal
import ru.zveron.service.api.review.ReviewService
import ru.zveron.setProfileInfoRequest
import ru.zveron.setSettingsRequest
import java.time.Instant

class ProfileServiceExternalTest : ProfileTest() {

    @Autowired
    lateinit var service: ProfileServiceExternal

    @Autowired
    lateinit var profileRepository: ProfileRepository

    @Autowired
    lateinit var settingsRepository: SettingsRepository

    @Autowired
    lateinit var contactRepository: ContactRepository

    @TestConfiguration
    class ProfileServiceExternalTestConfiguration {
        @Bean
        fun addressService() = mockk<AddressService>()

        @Bean
        fun blacklistService() = mockk<BlacklistService>()

        @Bean
        fun lotService() = mockk<LotService>()

        @Bean
        fun reviewService() = mockk<ReviewService>()
    }

    @Autowired
    lateinit var addressService: AddressService

    @Autowired
    lateinit var blacklistService: BlacklistService

    @Autowired
    lateinit var lotService: LotService

    @Autowired
    lateinit var reviewService: ReviewService

    @ParameterizedTest
    @ValueSource(longs = [0, 10])
    fun `Get profile page`(authorizedId: Long) {
        val now = Instant.now()
        val (id, addressId) = PropsGenerator.generateNIds(2)
        val expectedProfile = ProfileGenerator.generateProfile(id, now, addressId)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        ContactsGenerator.generateContact(expectedProfile, addPhone = true)
        profileRepository.save(expectedProfile)
        val request = getProfilePageRequest {
            requestedProfileId = id
            authorizedProfileId = authorizedId
        }
        coEvery { blacklistService.existsInBlacklist(id, authorizedId) } returns false
        val activeLot = LotsGenerator.generateLot(false)
        val closedLot = LotsGenerator.generateLot(false)
        coEvery { lotService.getLotsBySellerId(id) } returns profileLotsResponse {
            activateLots.add(activeLot)
            inactivateLots.add(closedLot)
        }
        val address = generateAddress(addressId)
        coEvery { addressService.getById(addressId) } returns address
        val rating = PropsGenerator.generateDouble()
        coEvery { reviewService.getRating(id) } returns rating

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
    fun `Get profile page if authorized and in blacklist`() {
        val now = Instant.now()
        val (id, authorizedProfile, addressId) = PropsGenerator.generateNIds(3)
        val expectedProfile = ProfileGenerator.generateProfile(id, now, addressId)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        ContactsGenerator.generateContact(expectedProfile, addPhone = true)
        profileRepository.save(expectedProfile)
        val request = getProfilePageRequest {
            requestedProfileId = id
            authorizedProfileId = authorizedProfile
        }
        coEvery { blacklistService.existsInBlacklist(id, authorizedProfile) } returns true

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
    fun `Get profile page if wrong id`() {
        val id = PropsGenerator.generateUserId()
        val request = getProfilePageRequest { requestedProfileId = id }

        val exception = shouldThrow<ProfileException> {
            runBlocking {
                service.getProfilePage(request)
            }
        }
        exception.message shouldBe "Profile with id: $id doesn't exist"
    }

    @Test
    fun `Get profile for owner`() {
        val now = Instant.now()
        val (id, addressId) = PropsGenerator.generateNIds(2)
        val expectedProfile = ProfileGenerator.generateProfile(id, now, addressId)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        ContactsGenerator.generateContact(expectedProfile, addPhone = true)
        profileRepository.save(expectedProfile)
        val request = getProfileInfoRequest { this.id = id }
        val address = generateAddress(addressId)
        coEvery { addressService.getById(addressId) } returns address
        val rating = PropsGenerator.generateDouble()
        coEvery { reviewService.getRating(id) } returns rating

        runBlocking {
            val response = service.getProfileInfo(request)

            response responseShouldBe expectedProfile
            response.address addressShouldBe address
            response.rating shouldBe rating
        }
    }

    @Test
    fun `Get profile for owner and id is incorrect`() {
        val id = PropsGenerator.generateUserId()
        val request = getProfileInfoRequest { this.id = id }

        val exception = shouldThrow<ProfileException> {
            runBlocking {
                service.getProfileInfo(request)
            }
        }
        exception.message shouldBe "Profile with id: $id doesn't exist"
    }

    @Test
    fun `Set profile info`() {
        val now = Instant.now()
        val (id, addressId) = PropsGenerator.generateNIds(2)
        val expectedProfile = ProfileGenerator.generateProfile(id, now, addressId)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        ContactsGenerator.generateContact(expectedProfile, addPhone = true)
        profileRepository.save(expectedProfile)

        val request = setProfileInfoRequest {
            this.id = id
            name = PropsGenerator.generateString(10)
            surname = PropsGenerator.generateString(10)
            imageId = PropsGenerator.generateUserId()
            address = generateAddress()
        }
        val newAddressId = PropsGenerator.generateUserId()
        coEvery { addressService.saveIfNotExists(AddressMapper.address2Request(request.address)) } returns addressResponse {
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
    fun `Set profile info and id is incorrect`() {
        val id = PropsGenerator.generateUserId()
        val request = setProfileInfoRequest { this.id = id }

        val exception = shouldThrow<ProfileException> {
            runBlocking {
                service.setProfileInfo(request)
            }
        }
        exception.message shouldBe "Profile with id: $id doesn't exist"
    }

    @Test
    fun `Get channel types`() {
        val now = Instant.now()
        val (id, addressId) = PropsGenerator.generateNIds(2)
        val expectedProfile = ProfileGenerator.generateProfile(id, now, addressId)
        val settings = SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        ContactsGenerator.generateContact(expectedProfile, addPhone = true)
        profileRepository.save(expectedProfile)
        val request = getChannelTypesRequest { this.id = id }

        runBlocking {
            val response = service.getChannelTypes(request)

            response.channelsList channelsShouldBe settings.channels
        }
    }

    @Test
    fun `Get channel types and id is incorrect`() {
        val id = PropsGenerator.generateUserId()
        val request = getChannelTypesRequest { this.id = id }

        val exception = shouldThrow<ProfileException> {
            runBlocking {
                service.getChannelTypes(request)
            }
        }
        exception.message shouldBe "Profile with id: $id doesn't exist"
    }

    @Test
    fun `Get links`() {
        val now = Instant.now()
        val (id, addressId) = PropsGenerator.generateNIds(2)
        val expectedProfile = ProfileGenerator.generateProfile(id, now, addressId)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        val contacts = ContactsGenerator.generateContact(expectedProfile, addPhone = true)
        profileRepository.save(expectedProfile)
        val request = getLinksRequest { this.id = id }

        runBlocking {
            val links = service.getLinks(request)

            links linksShouldBe contacts
        }
    }

    @Test
    fun `Get links and id is incorrect`() {
        val id = PropsGenerator.generateUserId()
        val request = getLinksRequest { this.id = id }

        val exception = shouldThrow<ProfileException> {
            runBlocking {
                service.getLinks(request)
            }
        }
        exception.message shouldBe "Profile with id: $id doesn't exist"
    }

    @Test
    fun `Get settings`() {
        val now = Instant.now()
        val (id, addressId) = PropsGenerator.generateNIds(2)
        val expectedProfile = ProfileGenerator.generateProfile(id, now)
        val settings =
            SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true, addressId = addressId)
        ContactsGenerator.generateContact(expectedProfile, addPhone = true)
        profileRepository.save(expectedProfile)
        val request = getSettingsRequest { this.id = id }
        val address = generateAddress(addressId)
        coEvery { addressService.getById(addressId) } returns address

        runBlocking {
            val response = service.getSettings(request)

            response.channelsList channelsShouldBe settings.channels
            response.address addressShouldBe address
        }
    }

    @Test
    fun `Get settings and id is incorrect`() {
        val id = PropsGenerator.generateUserId()
        val request = getLinksRequest { this.id = id }

        val exception = shouldThrow<ProfileException> {
            runBlocking {
                service.getLinks(request)
            }
        }
        exception.message shouldBe "Profile with id: $id doesn't exist"
    }

    @Test
    fun `Set settings`() {
        val now = Instant.now()
        val (id, addressId) = PropsGenerator.generateNIds(2)
        val expectedProfile = ProfileGenerator.generateProfile(id, now)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true, addressId = addressId)
        ContactsGenerator.generateContact(expectedProfile, addPhone = true, addVk = true, addGmail = true)
        profileRepository.save(expectedProfile)
        val request = setSettingsRequest {
            this.id = id
            address = generateAddress()
            channels.addAll(listOf(ChannelType.VK, ChannelType.GOOGLE))
        }
        coEvery { addressService.saveIfNotExists(AddressMapper.address2Request(request.address)) } returns addressResponse {
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
    fun `Set settings if channels is missed`() {
        val now = Instant.now()
        val (id, addressId) = PropsGenerator.generateNIds(2)
        val expectedProfile = ProfileGenerator.generateProfile(id, now)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true, addressId = addressId)
        ContactsGenerator.generateContact(expectedProfile, addPhone = true, addVk = true)
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
    fun `Set settings if wrong number of channels`() {
        val now = Instant.now()
        val (id, addressId) = PropsGenerator.generateNIds(2)
        val expectedProfile = ProfileGenerator.generateProfile(id, now)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true, addressId = addressId)
        ContactsGenerator.generateContact(expectedProfile, addPhone = true, addVk = true)
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
    fun `Set settings and id is incorrect`() {
        val id = PropsGenerator.generateUserId()
        val request = setSettingsRequest { this.id = id }

        val exception = shouldThrow<ProfileException> {
            runBlocking {
                service.setSettings(request)
            }
        }
        exception.message shouldBe "Profile with id: $id doesn't exist"
    }

    @Test
    fun `Delete profile`() {
        val now = Instant.now()
        val (id, addressId) = PropsGenerator.generateNIds(2)
        val expectedProfile = ProfileGenerator.generateProfile(id, now)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true, addressId = addressId)
        ContactsGenerator.generateContact(expectedProfile, addPhone = true, addVk = true)
        profileRepository.save(expectedProfile)
        val request = deleteProfileRequest { this.id = id }

        runBlocking {
            service.deleteProfile(request)
        }

        profileRepository.findById(id).isPresent shouldBe false
        settingsRepository.findById(id).isPresent shouldBe false
        contactRepository.findById(id).isPresent shouldBe false

        // TODO: service should write to kafka
    }
}