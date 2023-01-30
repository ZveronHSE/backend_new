package ru.zveron.service.api

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.ProfileTest
import ru.zveron.commons.assertions.contactShouldBe
import ru.zveron.commons.assertions.profileShouldBe
import ru.zveron.commons.assertions.responseShouldBe
import ru.zveron.commons.assertions.settingsShouldBe
import ru.zveron.commons.generator.ContactsGenerator
import ru.zveron.commons.generator.ContactsGenerator.generateLinks
import ru.zveron.commons.generator.ProfileGenerator
import ru.zveron.commons.generator.PropsGenerator
import ru.zveron.commons.generator.SettingsGenerator
import ru.zveron.contract.profile.createProfileRequest
import ru.zveron.contract.profile.getProfileByChannelRequest
import ru.zveron.contract.profile.getProfileRequest
import ru.zveron.contract.profile.getProfileWithContactsRequest
import ru.zveron.contract.profile.model.ChannelType
import ru.zveron.contract.profile.updateContactsRequest
import ru.zveron.entity.Profile
import ru.zveron.exception.ProfileException
import ru.zveron.exception.ProfileNotFoundException
import ru.zveron.repository.ContactRepository
import ru.zveron.repository.ProfileRepository
import java.time.Instant

class ProfileServiceInternalTest : ProfileTest() {

    @Autowired
    lateinit var service: ProfileServiceInternal

    @Autowired
    lateinit var profileRepository: ProfileRepository

    @Autowired
    lateinit var contactRepository: ContactRepository

    @Test
    fun `createProfile whe nrequest is correct`() {
        val now = Instant.now()
        val id = PropsGenerator.generateUserId()
        val expectedProfile = ProfileGenerator.generateProfile(id, now)
        val expectedSettings = SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        val expectedContact = ContactsGenerator.generateContact(expectedProfile, addPhone = true)
        val request = generateCreateProfileRequest(expectedProfile)

        runBlocking {
            service.createProfile(request)

            val actualProfile = profileRepository.findById(id).get()
            actualProfile profileShouldBe expectedProfile
            actualProfile.settings settingsShouldBe expectedSettings
            actualProfile.contact contactShouldBe expectedContact
        }
    }

    @Test
    fun `createProfile when number of channels is incorrect`() {
        val now = Instant.now()
        val id = PropsGenerator.generateUserId()
        val expectedProfile = ProfileGenerator.generateProfile(id, now)
        ContactsGenerator.generateContact(expectedProfile, addPhone = true, addVk = true)
        val request = generateCreateProfileRequest(expectedProfile)

        val exception = shouldThrow<ProfileException> {
            runBlocking {
                service.createProfile(request)
            }
        }
        exception.message shouldBe "Invalid number of communication ways. Expected 1 or 2, but was: 3"
    }

    @Test
    fun `createProfile when links are incorrect`() {
        val now = Instant.now()
        val id = PropsGenerator.generateUserId()
        val expectedProfile = ProfileGenerator.generateProfile(id, now)
        SettingsGenerator.generateSettings(expectedProfile, addVk = true, addChat = true)
        expectedProfile.contact = ContactsGenerator.generateContact(expectedProfile, addVk = true).copy(vkId = "")
        val request = generateCreateProfileRequest(expectedProfile)

        val exception = shouldThrow<ProfileException> {
            runBlocking {
                service.createProfile(request)
            }
        }
        exception.message shouldBe "Vk id and ref should be both present or missed"
    }

    @Test
    fun `createProfile when id is duplicated`() {
        val now = Instant.now()
        val id = PropsGenerator.generateUserId()
        val expectedProfile = ProfileGenerator.generateProfile(id, now)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        ContactsGenerator.generateContact(expectedProfile, addPhone = true)
        profileRepository.save(expectedProfile)
        val request = generateCreateProfileRequest(expectedProfile)

        val exception = shouldThrow<ProfileException> {
            runBlocking {
                service.createProfile(request)
            }
        }
        exception.message shouldBe "Profile with id: $id already exists"
    }

    @Test
    fun `getProfile when request is correct`() {
        val now = Instant.now()
        val id = PropsGenerator.generateUserId()
        val expectedProfile = ProfileGenerator.generateProfile(id, now)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        ContactsGenerator.generateContact(expectedProfile, addPhone = true)
        profileRepository.save(expectedProfile)
        val request = getProfileRequest { this.id = id }

        runBlocking {
            val response = service.getProfile(request)

            response responseShouldBe expectedProfile
        }
    }

    @Test
    fun `getProfile when it does not exist`() {
        val id = PropsGenerator.generateUserId()
        val request = getProfileRequest { this.id = id }

        val exception = shouldThrow<ProfileNotFoundException> {
            runBlocking {
                service.getProfile(request)
            }
        }
        exception.message shouldBe "Profile with id: $id doesn't exist"
    }

    @Test
    fun `getProfileWithContacts if it exists`() {
        val now = Instant.now()
        val id = PropsGenerator.generateUserId()
        val expectedProfile = ProfileGenerator.generateProfile(id, now)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        ContactsGenerator.generateContact(expectedProfile, addPhone = true)
        profileRepository.save(expectedProfile)
        val request = getProfileWithContactsRequest { this.id = id }

        runBlocking {
            val response = service.getProfileWithContacts(request)

            response responseShouldBe expectedProfile
        }
    }

    @Test
    fun `getProfileWithContacts when it does not exist`() {
        val id = PropsGenerator.generateUserId()
        val request = getProfileWithContactsRequest { this.id = id }

        val exception = shouldThrow<ProfileNotFoundException> {
            runBlocking {
                service.getProfileWithContacts(request)
            }
        }
        exception.message shouldBe "Profile with id: $id doesn't exist"
    }

    @Test
    fun `updateContacts when request is correct`() {
        val now = Instant.now()
        val id = PropsGenerator.generateUserId()
        val expectedProfile = ProfileGenerator.generateProfile(id, now)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        ContactsGenerator.generateContact(expectedProfile, addPhone = true)
        profileRepository.save(expectedProfile)
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

        contactRepository.findById(id).get().gmail shouldBe request.links.gmail.email
    }

    @Test
    fun `updateContacts when profile does not exist`() {
        val id = PropsGenerator.generateUserId()
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
    }

    @Test
    fun `updateContacts when chat channel type is selected`() {
        val now = Instant.now()
        val id = PropsGenerator.generateUserId()
        val expectedProfile = ProfileGenerator.generateProfile(id, now)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        ContactsGenerator.generateContact(expectedProfile, addPhone = true)
        profileRepository.save(expectedProfile)
        val request =
            generateUpdateContactsRequest(id, ChannelType.CHAT)

        val exception = shouldThrow<ProfileException> {
            runBlocking {
                service.updateContacts(request)
            }
        }
        exception.message shouldBe "Chat channel type don't need to be added to contacts"
    }

    @Test
    fun `profileExistsByLink if correct id`() {
        val now = Instant.now()
        val id = PropsGenerator.generateUserId()
        val expectedProfile = ProfileGenerator.generateProfile(id, now)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        val contact = ContactsGenerator.generateContact(expectedProfile, addPhone = true)
        profileRepository.save(expectedProfile)
        val request = getProfileByChannelRequest {
            type = ChannelType.PHONE
            identifier = contact.phone
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
    }

    private fun generateCreateProfileRequest(profile: Profile) = createProfileRequest {
        authAccountId = profile.id
        name = profile.name
        surname = profile.surname
        imageId = profile.imageId
        links =
            profile.contact.let { generateLinks(it.phone, it.vkId, it.vkRef, it.additionalEmail, it.gmailId, it.gmail) }
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
