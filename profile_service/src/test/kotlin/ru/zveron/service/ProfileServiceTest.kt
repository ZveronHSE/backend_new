package ru.zveron.service

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import kotlinx.coroutines.runBlocking
import org.hibernate.LazyInitializationException
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.ProfileTest
import ru.zveron.commons.assertions.linksShouldBe
import ru.zveron.commons.generator.CommunicationLinksGenerator
import ru.zveron.commons.generator.ProfileGenerator
import ru.zveron.commons.generator.PropsGenerator
import ru.zveron.commons.generator.SettingsGenerator
import ru.zveron.domain.profile.ProfileInitializationType
import ru.zveron.mapper.ContactsMapper.toDto
import ru.zveron.repository.ProfileRepository
import java.time.Instant

class ProfileServiceTest : ProfileTest() {

    @Autowired
    lateinit var profileRepository: ProfileRepository

    @Autowired
    lateinit var profileService: ProfileService

    @Test
    fun `findByIdOrThrow should load communication links if specified correct type`() {
        val now = Instant.now()
        val (id, addressId) = PropsGenerator.generateNIds(2)
        val expectedProfile = ProfileGenerator.generateProfile(id, now, addressId)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        CommunicationLinksGenerator.generateLinks(expectedProfile, addPhone = true)
        profileRepository.save(expectedProfile)

        shouldNotThrow<LazyInitializationException> {
            runBlocking {
                val actualProfile = profileService.findByIdOrThrow(id, ProfileInitializationType.COMMUNICATION_LINKS)

                actualProfile.communicationLinks.toDto() linksShouldBe expectedProfile.communicationLinks.toDto()
            }
        }
    }

    @Test
    fun `findByIdOrThrow should throw if specified incorrect type`() {
        val now = Instant.now()
        val (id, addressId) = PropsGenerator.generateNIds(2)
        val expectedProfile = ProfileGenerator.generateProfile(id, now, addressId)
        SettingsGenerator.generateSettings(expectedProfile, addPhone = true, addChat = true)
        CommunicationLinksGenerator.generateLinks(expectedProfile, addPhone = true)
        profileRepository.save(expectedProfile)

        shouldThrow<LazyInitializationException> {
            runBlocking {
                val actualProfile = profileService.findByIdOrThrow(id, ProfileInitializationType.DEFAULT)

                actualProfile.communicationLinks.toDto() linksShouldBe expectedProfile.communicationLinks.toDto()
            }
        }
    }
}