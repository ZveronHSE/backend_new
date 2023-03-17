package ru.zveron.service.api

import io.grpc.Status
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.contract.profile.model.ChannelType
import ru.zveron.ProfileTest
import ru.zveron.commons.assertions.linksShouldBe
import ru.zveron.commons.assertions.profileShouldBe
import ru.zveron.commons.assertions.responseShouldBe
import ru.zveron.commons.assertions.settingsShouldBe
import ru.zveron.commons.generator.CommunicationLinksGenerator.generateLinks
import ru.zveron.commons.generator.ProfileGenerator
import ru.zveron.commons.generator.PropsGenerator
import ru.zveron.commons.generator.SettingsGenerator
import ru.zveron.contract.profile.createProfileRequest
import ru.zveron.contract.profile.existsByIdRequest
import ru.zveron.contract.profile.getProfileByChannelRequest
import ru.zveron.contract.profile.getProfileRequest
import ru.zveron.contract.profile.getProfileWithContactsRequest
import ru.zveron.contract.profile.updateContactsRequest
import ru.zveron.contract.profile.verifyProfileHashRequest
import ru.zveron.entity.Profile
import ru.zveron.exception.ProfileException
import ru.zveron.exception.ProfileNotFoundException
import ru.zveron.contract.profile.getProfilesSummaryRequest
import ru.zveron.repository.ProfileRepository
import ru.zveron.domain.link.GmailData
import ru.zveron.domain.link.VkData
import ru.zveron.mapper.ContactsMapper.toDto
import ru.zveron.mapper.ContactsMapper.toLinks
import ru.zveron.repository.CommunicationLinkRepository
import java.time.Instant

class ProfileServiceInternalTest : ProfileTest() {

    @Autowired
    lateinit var service: ProfileServiceInternal

    @Autowired
    lateinit var profileRepository: ProfileRepository

    @Autowired
    lateinit var communicationLinkRepository: CommunicationLinkRepository

    @Test
    fun `createProfile whe request is correct`() {
        val now = Instant.now()
        val expectedProfile = ProfileGenerator.generateProfile(now, addPassword = true)
        val expectedSettings = SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        val expectedLinks = generateLinks(expectedProfile, addPhone = true)
        val request = generateCreateProfileRequest(expectedProfile)

        runBlocking {
            val id = service.createProfile(request).id

            val actualProfile = profileRepository.findById(id).get()
            val actualCommunicationLinks = communicationLinkRepository.findAllByProfileId(id)
            actualProfile profileShouldBe expectedProfile.copy(id = id)
            actualProfile.settings settingsShouldBe expectedSettings.copy(id = id)
            actualCommunicationLinks.toDto() linksShouldBe expectedLinks
        }
    }

    @Test
    fun `createProfile when number of channels is incorrect`() {
        val now = Instant.now()
        val expectedProfile = ProfileGenerator.generateProfile(now)
        generateLinks(expectedProfile, addPhone = true, addVk = true)
        val request = generateCreateProfileRequest(expectedProfile)

        val exception = shouldThrow<ProfileException> {
            runBlocking {
                service.createProfile(request)
            }
        }
        exception.message shouldBe "Invalid number of communication ways. Expected 1 or 2, but was: 3"
        exception.code shouldBe Status.Code.INVALID_ARGUMENT
    }

    @Test
    fun `createProfile when links are incorrect`() {
        val now = Instant.now()
        val expectedProfile = ProfileGenerator.generateProfile(now)
        SettingsGenerator.generateSettings(expectedProfile, addVk = true, addChat = true)
        generateLinks(expectedProfile, addVk = true, skipVkRef = true)
        profileRepository.save(expectedProfile)
        val request = generateCreateProfileRequest(expectedProfile)

        val exception = shouldThrow<ProfileException> {
            runBlocking {
                service.createProfile(request)
            }
        }
        exception.message shouldBe "Vk id and ref should be both present or missed"
        exception.code shouldBe Status.Code.INVALID_ARGUMENT
    }

    @ParameterizedTest
    @CsvSource(value = ["true,false,false", "false,true,false", "false,false,true"])
    fun `createProfile when links are not unique`(vk: Boolean, gmail: Boolean, phone: Boolean) {
        val now = Instant.now()
        val expectedProfile = ProfileGenerator.generateProfile(now)
        SettingsGenerator.generateSettings(
            expectedProfile,
            addVk = vk,
            addGmail = gmail,
            addPhone = phone,
            addChat = true
        )
        generateLinks(expectedProfile, addVk = vk, addGmail = gmail, addPhone = phone)
        profileRepository.save(expectedProfile)
        val request = generateCreateProfileRequest(expectedProfile)

        val exception = shouldThrow<ProfileException> {
            runBlocking {
                service.createProfile(request)
            }
        }
        exception.message shouldBe "Specified communication link is already used"
        exception.code shouldBe Status.Code.ALREADY_EXISTS
    }

    @Test
    fun `getProfile when request is correct`() {
        val now = Instant.now()
        val expectedProfile = ProfileGenerator.generateProfile(now)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        generateLinks(expectedProfile, addPhone = true)
        val id = profileRepository.save(expectedProfile).id
        val request = getProfileRequest { this.id = id }

        runBlocking {
            val response = service.getProfile(request)

            response responseShouldBe expectedProfile
        }
    }

    @Test
    fun `getProfile when it does not exist`() {
        val id = PropsGenerator.generateLongId()
        val request = getProfileRequest { this.id = id }

        val exception = shouldThrow<ProfileNotFoundException> {
            runBlocking {
                service.getProfile(request)
            }
        }
        exception.message shouldBe "Profile with id: $id doesn't exist"
        exception.code shouldBe Status.Code.NOT_FOUND
    }

    @Test
    fun `getProfileWithContacts if it exists`() {
        val now = Instant.now()
        val expectedProfile = ProfileGenerator.generateProfile(now)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        generateLinks(expectedProfile, addPhone = true)
        val id = profileRepository.save(expectedProfile).id
        val request = getProfileWithContactsRequest { this.id = id }

        runBlocking {
            val response = service.getProfileWithContacts(request)

            response responseShouldBe expectedProfile
        }
    }

    @Test
    fun `getProfileWithContacts when it does not exist`() {
        val id = PropsGenerator.generateLongId()
        val request = getProfileWithContactsRequest { this.id = id }

        val exception = shouldThrow<ProfileNotFoundException> {
            runBlocking {
                service.getProfileWithContacts(request)
            }
        }
        exception.message shouldBe "Profile with id: $id doesn't exist"
        exception.code shouldBe Status.Code.NOT_FOUND
    }

    @Test
    fun `updateContacts when request is correct and add new link`() {
        val now = Instant.now()
        val expectedProfile = ProfileGenerator.generateProfile(now)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        generateLinks(expectedProfile, addPhone = true)
        val id = profileRepository.save(expectedProfile).id
        val request =
            generateUpdateContactsRequest(
                id,
                ChannelType.GOOGLE,
                gmailId = PropsGenerator.generateString(15),
                gmail = PropsGenerator.generateString(15)
            )

        runBlocking {
            service.updateContacts(request)
        }

        val gmail = communicationLinkRepository.findAllByProfileId(id).toDto().gmailLink!!
        gmail.communicationLinkId shouldBe request.links.gmail.id
        (gmail.data as GmailData)
            .email shouldBe request.links.gmail.email
    }

    @Test
    fun `updateContacts when request is correct and edit existed link`() {
        val now = Instant.now()
        val expectedProfile = ProfileGenerator.generateProfile(now)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        generateLinks(expectedProfile, addVk = true)
        val id = profileRepository.save(expectedProfile).id
        val request =
            generateUpdateContactsRequest(
                id,
                ChannelType.VK,
                vkId = PropsGenerator.generateString(15),
                vkRef = PropsGenerator.generateString(15),
                additionalEmail = PropsGenerator.generateString(15),
            )

        runBlocking {
            service.updateContacts(request)
        }

        val vk = communicationLinkRepository.findAllByProfileId(id).toDto().vkLink!!
        vk.communicationLinkId shouldBe request.links.vk.id
        (vk.data as VkData).apply {
            ref shouldBe request.links.vk.ref
            email shouldBe request.links.vk.email
        }
    }

    @Test
    fun `updateContacts when profile does not exist`() {
        val id = PropsGenerator.generateLongId()
        val request = generateUpdateContactsRequest(
            id,
            ChannelType.GOOGLE,
            gmailId = PropsGenerator.generateString(15),
            gmail = PropsGenerator.generateString(15)
        )

        val exception = shouldThrow<ProfileNotFoundException> {
            runBlocking {
                service.updateContacts(request)
            }
        }
        exception.message shouldBe "Profile with id: $id doesn't exist"
        exception.code shouldBe Status.Code.NOT_FOUND
    }

    @Test
    fun `updateContacts when chat channel type is selected`() {
        val now = Instant.now()
        val expectedProfile = ProfileGenerator.generateProfile(now)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        generateLinks(expectedProfile, addPhone = true)
        val id = profileRepository.save(expectedProfile).id
        val request =
            generateUpdateContactsRequest(id, ChannelType.CHAT)

        val exception = shouldThrow<ProfileException> {
            runBlocking {
                service.updateContacts(request)
            }
        }
        exception.message shouldBe "Chat channel type don't need to be added to contacts"
        exception.code shouldBe Status.Code.INVALID_ARGUMENT
    }

    @Test
    fun `profileExistsByLink if correct id`() {
        val now = Instant.now()
        val expectedProfile = ProfileGenerator.generateProfile(now)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        val linksDto = generateLinks(expectedProfile, addPhone = true)
        profileRepository.save(expectedProfile)
        val request = getProfileByChannelRequest {
            type = ChannelType.PHONE
            identifier = linksDto.phoneLink!!.communicationLinkId
        }

        runBlocking {
            val response = service.getProfileByChannel(request)

            response responseShouldBe expectedProfile
        }
    }

    @Test
    fun `profileExistsByLink if wrong id`() {
        val id = PropsGenerator.generateString(10)
        val channelType = ChannelType.VK
        val request = getProfileByChannelRequest {
            type = channelType
            identifier = id
        }

        val exception = shouldThrow<ProfileNotFoundException> {
            runBlocking {
                service.getProfileByChannel(request)
            }
        }
        exception.message shouldBe "Can't find profile by channel: $channelType and channel id: $id"
        exception.code shouldBe Status.Code.NOT_FOUND
    }

    @Test
    fun `verifyProfileHash if hash is valid`() {
        val now = Instant.now()
        val expectedProfile = ProfileGenerator.generateProfile(now, addPassword = true)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        val linksDto = generateLinks(expectedProfile, addPhone = true)
        profileRepository.save(expectedProfile)
        val request = verifyProfileHashRequest {
            phoneNumber = linksDto.phoneLink!!.communicationLinkId
            passwordHash = expectedProfile.passwordHash!!
        }

        runBlocking {
            val response = service.verifyProfileHash(request)

            response.isValidRequest shouldBe true
        }
    }

    @Test
    fun `verifyProfileHash if hash is invalid`() {
        val now = Instant.now()
        val expectedProfile = ProfileGenerator.generateProfile(now, addPassword = true)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        val linksDto = generateLinks(expectedProfile, addPhone = true)
        profileRepository.save(expectedProfile)
        val request = verifyProfileHashRequest {
            phoneNumber = linksDto.phoneLink!!.communicationLinkId
            passwordHash = "123"
        }

        runBlocking {
            val response = service.verifyProfileHash(request)

            response.isValidRequest shouldBe false
        }
    }

    @Test
    fun `verifyProfileHash if profile does not exist`() {
        val now = Instant.now()
        val expectedProfile = ProfileGenerator.generateProfile(now)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        val linksDto = generateLinks(expectedProfile, addPhone = true)
        val request = verifyProfileHashRequest {
            phoneNumber = linksDto.phoneLink!!.communicationLinkId
            passwordHash = "123"
        }

        runBlocking {
            val response = service.verifyProfileHash(request)

            response.isValidRequest shouldBe false
        }
    }

    @Test
    fun `verifyProfileHash if profile does not have a password`() {
        val now = Instant.now()
        val expectedProfile = ProfileGenerator.generateProfile(now)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        val linksDto = generateLinks(expectedProfile, addPhone = true)
        profileRepository.save(expectedProfile)
        val request = verifyProfileHashRequest {
            phoneNumber = linksDto.phoneLink!!.communicationLinkId
            passwordHash = "123"
        }

        val exception = shouldThrow<ProfileException> {
            runBlocking {
                service.verifyProfileHash(request)
            }
        }
        exception.message shouldBe "Password hasn't been set yet for this profile"
        exception.code shouldBe Status.Code.FAILED_PRECONDITION
    }

    @Test
    fun `getProfilesSummary if request is correct`() {
        val now = Instant.now()
        val profiles = mutableListOf<Profile>()
        for (id in 0..2) {
            val expectedProfile = ProfileGenerator.generateProfile(now)
            SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
            generateLinks(expectedProfile, addPhone = true)
            profiles.add(profileRepository.save(expectedProfile))
        }
        val request = getProfilesSummaryRequest {
            this.ids.addAll(listOf(profiles[0].id, profiles[1].id))
        }

        runBlocking {
            val response = service.getProfilesSummary(request).profilesList
            response.first { it.id == profiles[0].id } profileShouldBe profiles[0]
            response.first { it.id == profiles[1].id  } profileShouldBe profiles[1]
        }
    }

    @Test
    fun `ExistsById if exists`() {
        val now = Instant.now()
        val expectedProfile = ProfileGenerator.generateProfile(now)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        generateLinks(expectedProfile, addVk = true)
        val id = profileRepository.save(expectedProfile).id

        runBlocking {
            service.existsById(existsByIdRequest { this.id = id }).exists shouldBe true
        }
    }

    @Test
    fun `ExistsById if not exists`() {
        runBlocking {
            service.existsById(existsByIdRequest { this.id = PropsGenerator.generateLongId() }).exists shouldBe false
        }
    }

    private fun generateCreateProfileRequest(profile: Profile) = createProfileRequest {
        name = profile.name
        surname = profile.surname
        imageId = profile.imageId
        links = profile.communicationLinks.toDto().toLinks()
        passwordHash = profile.passwordHash ?: ""
    }

    private fun generateUpdateContactsRequest(
        id: Long,
        type: ChannelType,
        phone: String = "",
        vkId: String = "",
        vkRef: String = "",
        additionalEmail: String = "",
        gmailId: String = "",
        gmail: String = ""
    ) = updateContactsRequest {
        profileId = id
        this.type = type
        links = generateLinks(phone, vkId, vkRef, additionalEmail, gmailId, gmail)
    }
}