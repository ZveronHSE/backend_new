package ru.zveron.service.api

import com.google.protobuf.Empty
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import ru.zveron.ProfileTest
import ru.zveron.commons.assertions.addressShouldBe
import ru.zveron.commons.assertions.channelsShouldBe
import ru.zveron.commons.assertions.linksShouldBe
import ru.zveron.commons.assertions.profileShouldBe
import ru.zveron.commons.assertions.responseShouldBe
import ru.zveron.commons.assertions.responseShouldBeBlockedAnd
import ru.zveron.commons.generator.AddressGenerator.generateAddress
import ru.zveron.commons.generator.CommunicationLinksGenerator
import ru.zveron.commons.generator.LotsGenerator
import ru.zveron.commons.generator.ProfileGenerator
import ru.zveron.commons.generator.PropsGenerator
import ru.zveron.commons.generator.SettingsGenerator
import ru.zveron.contract.address.addressResponse
import ru.zveron.contract.core.Status
import ru.zveron.contract.lot.profileLotsResponse
import ru.zveron.contract.profile.SetSettingsRequest
import ru.zveron.contract.profile.getProfilePageRequest
import ru.zveron.contract.profile.model.ChannelType
import ru.zveron.contract.profile.setProfileInfoRequest
import ru.zveron.contract.profile.setSettingsRequest
import ru.zveron.exception.ProfileException
import ru.zveron.exception.ProfileNotFoundException
import ru.zveron.exception.ProfileUnauthenticated
import ru.zveron.library.grpc.interceptor.model.MetadataElement
import ru.zveron.library.grpc.model.Metadata
import ru.zveron.mapper.AddressMapper.toProfileAddress
import ru.zveron.mapper.AddressMapper.toRequest
import ru.zveron.repository.CommunicationLinkRepository
import ru.zveron.repository.ProfileRepository
import ru.zveron.repository.SettingsRepository
import ru.zveron.service.client.address.AddressClient
import ru.zveron.service.client.blakclist.BlacklistClient
import ru.zveron.service.client.lot.LotClient
import ru.zveron.service.client.review.ReviewClient
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
        val addressId = PropsGenerator.generateLongId()
        val expectedProfile = ProfileGenerator.generateProfile(now, addressId)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        CommunicationLinksGenerator.generateLinks(expectedProfile, addPhone = true)
        val id = profileRepository.save(expectedProfile).id
        val request = getProfilePageRequest {
            requestedProfileId = id
        }
        coEvery { blacklistClient.existsInBlacklist(id, authorizedId) } returns false
        val activeLot = LotsGenerator.generateLot(favorite = false, active = true)
        val closedLot = LotsGenerator.generateLot(favorite = false, active = false)
        coEvery { lotClient.getLotsBySellerId(id, authorizedId) } returns profileLotsResponse {
            activateLots.add(activeLot)
            inactivateLots.add(closedLot)
        }
        val address = generateAddress(addressId)
        coEvery { addressClient.getById(addressId) } returns address
        val rating = PropsGenerator.generateDouble()
        coEvery { reviewClient.getRating(id) } returns rating

        runBlocking(MetadataElement(Metadata(authorizedId))) {
            val response = service.getProfilePage(request)

            response responseShouldBe expectedProfile
            response.address shouldBe address.toProfileAddress()
            response.rating shouldBe rating
            //TODO: response.reviewNumber should be (?)
            response.activeLotsList.apply {
                size shouldBe 1
                first() shouldBe activeLot
                first().status shouldBe Status.ACTIVE
            }
            response.closedLotsList.apply {
                size shouldBe 1
                first() shouldBe closedLot
                first().status shouldBe Status.CLOSED
            }
        }
    }

    @Test
    fun `getProfilePage when authorized profile page requested`() {
        val now = Instant.now()
        val addressId = PropsGenerator.generateLongId()
        val expectedProfile = ProfileGenerator.generateProfile(now, addressId)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        CommunicationLinksGenerator.generateLinks(expectedProfile, addPhone = true)
        val id = profileRepository.save(expectedProfile).id
        val request = getProfilePageRequest {
            requestedProfileId = id
        }
        val activeLot = LotsGenerator.generateLot(favorite = false, active = true)
        val closedLot = LotsGenerator.generateLot(favorite = false, active = false)
        coEvery { lotClient.getLotsBySellerId(id, id) } returns profileLotsResponse {
            activateLots.add(activeLot)
            inactivateLots.add(closedLot)
        }
        val address = generateAddress(addressId)
        coEvery { addressClient.getById(addressId) } returns address
        val rating = PropsGenerator.generateDouble()
        coEvery { reviewClient.getRating(id) } returns rating

        runBlocking(MetadataElement(Metadata(id))) {
            val response = service.getProfilePage(request)

            response responseShouldBe expectedProfile
            response.address shouldBe address.toProfileAddress()
            response.rating shouldBe rating
            //TODO: response.reviewNumber should be (?)
            response.activeLotsList.apply {
                size shouldBe 1
                first() shouldBe activeLot
                first().status shouldBe Status.ACTIVE
            }
            response.closedLotsList.apply {
                size shouldBe 1
                first() shouldBe closedLot
                first().status shouldBe Status.CLOSED
            }
        }
    }

    @Test
    fun `getProfilePage when authorized and in blacklist`() {
        val now = Instant.now()
        val (authorizedProfile, addressId) = PropsGenerator.generateNIds(2)
        val expectedProfile = ProfileGenerator.generateProfile(now, addressId)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        CommunicationLinksGenerator.generateLinks(expectedProfile, addPhone = true)
        val id = profileRepository.save(expectedProfile).id
        val request = getProfilePageRequest {
            requestedProfileId = id
        }
        coEvery { blacklistClient.existsInBlacklist(id, authorizedProfile) } returns true

        runBlocking(MetadataElement(Metadata(authorizedProfile))) {
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
        val id = PropsGenerator.generateLongId()
        val request = getProfilePageRequest { requestedProfileId = id }

        val exception = shouldThrow<ProfileNotFoundException> {
            runBlocking {
                service.getProfilePage(request)
            }
        }
        exception.message shouldBe "Profile with id: $id doesn't exist"
        exception.code shouldBe io.grpc.Status.Code.NOT_FOUND
    }

    @Test
    fun `getProfileInfo when request is correct`() {
        val now = Instant.now()
        val addressId = PropsGenerator.generateLongId()
        val expectedProfile = ProfileGenerator.generateProfile(now, addressId)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        CommunicationLinksGenerator.generateLinks(expectedProfile, addPhone = true)
        val id = profileRepository.save(expectedProfile).id
        val request = Empty.getDefaultInstance()
        val address = generateAddress(addressId)
        coEvery { addressClient.getById(addressId) } returns address
        val rating = PropsGenerator.generateDouble()
        coEvery { reviewClient.getRating(id) } returns rating

        runBlocking(MetadataElement(Metadata(id))) {
            val response = service.getProfileInfo(request)

            response responseShouldBe expectedProfile
            response.address addressShouldBe address
            response.rating shouldBe rating
        }
    }

    @Test
    fun `getProfileInfo when unauthenticated`() {
        val request = Empty.getDefaultInstance()

        val exception = shouldThrow<ProfileUnauthenticated> {
            runBlocking {
                service.getProfileInfo(request)
            }
        }
        exception.message shouldBe "Authentication required"
    }

    @Test
    fun `setProfileInfo when request is correct`() {
        val now = Instant.now()
        val addressId = PropsGenerator.generateLongId()
        val expectedProfile = ProfileGenerator.generateProfile(now, addressId)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        CommunicationLinksGenerator.generateLinks(expectedProfile, addPhone = true)
        val id = profileRepository.save(expectedProfile).id

        val request = setProfileInfoRequest {
            name = PropsGenerator.generateString(10)
            surname = PropsGenerator.generateString(10)
            imageUrl = PropsGenerator.generateImageUrl()
            address = generateAddress()
        }
        val newAddressId = PropsGenerator.generateLongId()
        coEvery { addressClient.saveIfNotExists(request.address.toRequest()) } returns addressResponse {
            this.id = newAddressId
        }

        runBlocking(MetadataElement(Metadata(id))) {
            service.setProfileInfo(request)
        }

        val actualProfile = profileRepository.findById(id).get()
        actualProfile profileShouldBe request
        actualProfile.addressId shouldBe newAddressId
    }

    @Test
    fun `setProfileInfo when unauthenticated`() {
        val request = setProfileInfoRequest { }

        val exception = shouldThrow<ProfileUnauthenticated> {
            runBlocking {
                service.setProfileInfo(request)
            }
        }
        exception.message shouldBe "Authentication required"
        exception.code shouldBe io.grpc.Status.Code.UNAUTHENTICATED
    }

    @Test
    fun `getChannelTypes when request is correct`() {
        val now = Instant.now()
        val addressId = PropsGenerator.generateLongId()
        val expectedProfile = ProfileGenerator.generateProfile(now, addressId)
        val settings = SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        CommunicationLinksGenerator.generateLinks(expectedProfile, addPhone = true)
        val id = profileRepository.save(expectedProfile).id
        val request = Empty.getDefaultInstance()

        runBlocking(MetadataElement(Metadata(id))) {
            val response = service.getChannelTypes(request)

            response.channelsList channelsShouldBe settings.channels
        }
    }

    @Test
    fun `getChannelTypes when unauthenticated`() {
        val request = Empty.getDefaultInstance()

        val exception = shouldThrow<ProfileUnauthenticated> {
            runBlocking {
                service.getChannelTypes(request)
            }
        }
        exception.message shouldBe "Authentication required"
        exception.code shouldBe io.grpc.Status.Code.UNAUTHENTICATED
    }

    @Test
    fun `getLinks when request is correct`() {
        val now = Instant.now()
        val addressId = PropsGenerator.generateLongId()
        val expectedProfile = ProfileGenerator.generateProfile(now, addressId)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        val contacts = CommunicationLinksGenerator.generateLinks(expectedProfile, addPhone = true)
        val id = profileRepository.save(expectedProfile).id
        val request = Empty.getDefaultInstance()

        runBlocking(MetadataElement(Metadata(id))) {
            val links = service.getLinks(request)

            links linksShouldBe contacts
        }
    }

    @Test
    fun `getLinks when unauthenticated`() {
        val request = Empty.getDefaultInstance()

        val exception = shouldThrow<ProfileUnauthenticated> {
            runBlocking {
                service.getLinks(request)
            }
        }
        exception.message shouldBe "Authentication required"
        exception.code shouldBe io.grpc.Status.Code.UNAUTHENTICATED
    }

    @Test
    fun `getSettings when request is correct`() {
        val now = Instant.now()
        val addressId = PropsGenerator.generateLongId()
        val expectedProfile = ProfileGenerator.generateProfile(now)
        val settings =
            SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true, addressId = addressId)
        CommunicationLinksGenerator.generateLinks(expectedProfile, addPhone = true)
        val id = profileRepository.save(expectedProfile).id
        val request = Empty.getDefaultInstance()
        val address = generateAddress(addressId)
        coEvery { addressClient.getById(addressId) } returns address

        runBlocking(MetadataElement(Metadata(id))) {
            val response = service.getSettings(request)

            response.channelsList channelsShouldBe settings.channels
            response.address addressShouldBe address
        }
    }

    @Test
    fun `getSettings when unauthenticated`() {
        val request = Empty.getDefaultInstance()

        val exception = shouldThrow<ProfileUnauthenticated> {
            runBlocking {
                service.getLinks(request)
            }
        }
        exception.message shouldBe "Authentication required"
        exception.code shouldBe io.grpc.Status.Code.UNAUTHENTICATED
    }

    @Test
    fun `setSettings when request is correct`() {
        val now = Instant.now()
        val addressId = PropsGenerator.generateLongId()
        val expectedProfile = ProfileGenerator.generateProfile(now)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true, addressId = addressId)
        CommunicationLinksGenerator.generateLinks(expectedProfile, addPhone = true, addVk = true, addGmail = true)
        val id = profileRepository.save(expectedProfile).id
        val request = setSettingsRequest {
            address = generateAddress()
            channels.addAll(listOf(ChannelType.VK, ChannelType.GOOGLE))
        }
        coEvery { addressClient.saveIfNotExists(request.address.toRequest()) } returns addressResponse {
            this.id = addressId
        }

        runBlocking(MetadataElement(Metadata(id))) {
            service.setSettings(request)

            val actualSettings = settingsRepository.findById(id).get()
            actualSettings.channels channelsShouldBe request.channelsList
            actualSettings.searchAddressId shouldBe addressId
        }
    }

    @ParameterizedTest
    @CsvSource(value = ["true,false,false,false", "false,true,false,false", "false,false,true,false", "false,false,false,true"])
    fun `setSettings when channels is missed`(vk: Boolean, gmail: Boolean, phone: Boolean, mailRu: Boolean) {
        var additionalChannelType : ChannelType? = null
        var linkName = "gmail"
        when {
            vk -> { additionalChannelType = ChannelType.VK; linkName = "vk"}
            gmail -> { additionalChannelType = ChannelType.GOOGLE; linkName = "gmail"}
            mailRu -> { additionalChannelType = ChannelType.MAILRU; linkName = "mail.ru"}
            phone -> { additionalChannelType = ChannelType.PHONE; linkName = "phone"}
        }
        val now = Instant.now()
        val addressId = PropsGenerator.generateLongId()
        val expectedProfile = ProfileGenerator.generateProfile(now)
        SettingsGenerator.generateSettings(expectedProfile, addChat = true, addressId = addressId)
        CommunicationLinksGenerator.generateLinks(expectedProfile, addPhone = !phone, addVk = !vk, addGmail = !gmail, addMailRy = !mailRu)
        val id = profileRepository.save(expectedProfile).id
        val request = setSettingsRequest {
            address = generateAddress()
            channels.addAll(listOf(ChannelType.CHAT, additionalChannelType!!))
        }

        val exception = shouldThrow<ProfileException> {
            runBlocking(MetadataElement(Metadata(id))) {
                service.setSettings(request)
            }
        }
        exception.message shouldBe "Can't use $linkName as communication channel because link is missed"
        exception.code shouldBe io.grpc.Status.Code.INVALID_ARGUMENT
    }

    @Test
    fun `setSettings when wrong number of channels`() {
        val now = Instant.now()
        val addressId = PropsGenerator.generateLongId()
        val expectedProfile = ProfileGenerator.generateProfile(now)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true, addressId = addressId)
        CommunicationLinksGenerator.generateLinks(expectedProfile, addPhone = true, addVk = true)
        val id = profileRepository.save(expectedProfile).id
        val request = setSettingsRequest {
            address = generateAddress()
        }

        val exception = shouldThrow<ProfileException> {
            runBlocking(MetadataElement(Metadata(id))) {
                service.setSettings(request)
            }
        }
        exception.message shouldBe "Invalid number of communication ways. Expected 1 or 2, but was: 0"
        exception.code shouldBe io.grpc.Status.Code.INVALID_ARGUMENT
    }

    @Test
    fun `setSettings when unauthenticated`() {
        val request = SetSettingsRequest.getDefaultInstance()

        val exception = shouldThrow<ProfileUnauthenticated> {
            runBlocking {
                service.setSettings(request)
            }
        }
        exception.message shouldBe "Authentication required"
        exception.code shouldBe io.grpc.Status.Code.UNAUTHENTICATED
    }

    @Test
    fun `deleteProfile when request is correct`() {
        val now = Instant.now()
        val addressId = PropsGenerator.generateLongId()
        val expectedProfile = ProfileGenerator.generateProfile(now)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true, addressId = addressId)
        CommunicationLinksGenerator.generateLinks(expectedProfile, addPhone = true, addVk = true)
        val id = profileRepository.save(expectedProfile).id
        val request = Empty.getDefaultInstance()

        runBlocking(MetadataElement(Metadata(id))) {
            service.deleteProfile(request)
        }

        profileRepository.findById(id).isPresent shouldBe false
        settingsRepository.findById(id).isPresent shouldBe false
        communicationLinkRepository.findAllByProfileId(id).size shouldBe 0

        // TODO: service should write to kafka
    }

    @Test
    fun `deleteProfile when unauthenticated`() {
        val request = Empty.getDefaultInstance()

        val exception = shouldThrow<ProfileUnauthenticated> {
            runBlocking {
                service.deleteProfile(request)
            }
        }
        exception.message shouldBe "Authentication required"
        exception.code shouldBe io.grpc.Status.Code.UNAUTHENTICATED
    }
}
