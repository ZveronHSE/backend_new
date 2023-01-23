package ru.zveron.service.profile

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.ChannelType
import ru.zveron.ProfileTest
import ru.zveron.commons.assertions.contactShouldBe
import ru.zveron.commons.assertions.profileShouldBe
import ru.zveron.commons.assertions.responseShouldBe
import ru.zveron.commons.assertions.settingsShouldBe
import ru.zveron.commons.generator.ContactsGenerator
import ru.zveron.commons.generator.ProfileGenerator
import ru.zveron.commons.generator.PropsGenerator
import ru.zveron.commons.generator.SettingsGenerator
import ru.zveron.createProfileRequest
import ru.zveron.entity.Profile
import ru.zveron.exception.ProfileException
import ru.zveron.getProfileRequest
import ru.zveron.getProfileWithContactsRequest
import ru.zveron.gmail
import ru.zveron.links
import ru.zveron.phone
import ru.zveron.repository.ContactRepository
import ru.zveron.repository.ProfileRepository
import ru.zveron.service.api.profile.ProfileServiceInternal
import ru.zveron.updateContactsRequest
import ru.zveron.vKLinks
import java.time.Instant

class ProfileServiceInternalTest : ProfileTest() {

    @Autowired
    lateinit var service: ProfileServiceInternal

    @Autowired
    lateinit var profileRepository: ProfileRepository

    @Autowired
    lateinit var contactRepository: ContactRepository

    @Test
    fun `Create new profile`() {
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
    fun `Create new profile and number of channels is incorrect`() {
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
    fun `Create profile with repeated id`() {
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
    fun `Get profile if it exists`() {
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
    fun `Get profile if it does not exist`() {
        val id = PropsGenerator.generateUserId()
        val request = getProfileRequest { this.id = id }

        val exception = shouldThrow<ProfileException> {
            runBlocking {
                service.getProfile(request)
            }
        }
        exception.message shouldBe "Profile with id: $id doesn't exist"
    }

    @Test
    fun `Get profile with contacts if it exists`() {
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
    fun `Get profile with contacts if it does not exist`() {
        val id = PropsGenerator.generateUserId()
        val request = getProfileWithContactsRequest { this.id = id }

        val exception = shouldThrow<ProfileException> {
            runBlocking {
                service.getProfileWithContacts(request)
            }
        }
        exception.message shouldBe "Profile with id: $id doesn't exist"
    }

    @Test
    fun `Update profile contacts`() {
        val now = Instant.now()
        val id = PropsGenerator.generateUserId()
        val expectedProfile = ProfileGenerator.generateProfile(id, now)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        ContactsGenerator.generateContact(expectedProfile, addPhone = true)
        profileRepository.save(expectedProfile)
        val request =
            generateUpdateContactsRequest(id, ChannelType.GOOGLE, gmail = PropsGenerator.generateString(15))

        runBlocking {
            service.updateContacts(request)
        }

        contactRepository.findById(id).get().gmail shouldBe request.links.gmail.email
    }

    @Test
    fun `Update profile if it does not exist`() {
        val id = PropsGenerator.generateUserId()
        val request = generateUpdateContactsRequest(id, ChannelType.GOOGLE, gmail = PropsGenerator.generateString(15))

        val exception = shouldThrow<ProfileException> {
            runBlocking {
                service.updateContacts(request)
            }
        }
        exception.message shouldBe "Profile with id: $id doesn't exist"
    }

    @Test
    fun `Update profile if chat channel type is selected`() {
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

    private fun generateCreateProfileRequest(profile: Profile) = createProfileRequest {
        authAccountId = profile.id
        name = profile.name
        surname = profile.surname
        imageId = profile.imageId
        links = profile.contact.let { generateLinks(it.phone, it.vkRef, it.additionalEmail, it.gmail) }
    }

    private fun generateUpdateContactsRequest(
        id: Long,
        type: ChannelType,
        phone: String = "",
        vkRef: String = "",
        additionalEmail: String = "",
        gmail: String = ""
    ) = updateContactsRequest {
        profileId = id
        this.type = type
        links = generateLinks(phone, vkRef, additionalEmail, gmail)
    }

    private fun generateLinks(
        phone: String,
        vkRef: String,
        additionalEmail: String,
        gmail: String
    ) = links {
        this.phone = phone {
            number = phone
        }
        vk = vKLinks {
            ref = vkRef
            email = additionalEmail
        }
        this.gmail = gmail {
            email = gmail
        }
    }
}