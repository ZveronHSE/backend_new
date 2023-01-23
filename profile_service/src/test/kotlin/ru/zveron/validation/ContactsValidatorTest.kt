package ru.zveron.validation

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import ru.zveron.commons.generator.ContactsGenerator
import ru.zveron.commons.generator.ProfileGenerator
import ru.zveron.commons.generator.PropsGenerator
import ru.zveron.domain.ChannelsDTO
import ru.zveron.exception.ProfileException
import java.time.Instant

class ContactsValidatorTest {

    companion object {
        @JvmStatic
        private fun getCorrectChanelTypes() = listOf(
            Arguments.of(ChannelsDTO(phone = true)),
            Arguments.of(ChannelsDTO(vk = true, gmail = true)),
        )

        @JvmStatic
        private fun getIncorrectChanelTypes() = listOf(
            Arguments.of(ChannelsDTO(), 0),
            Arguments.of(ChannelsDTO(phone = true, vk = true, gmail = true), 3),
            Arguments.of(ChannelsDTO(phone = true, vk = true, gmail = true, chat = true), 4),
        )
    }

    @ParameterizedTest
    @MethodSource("getCorrectChanelTypes")
    fun `Validate correct number of types`(ways: ChannelsDTO) {
        shouldNotThrow<ProfileException> {
            ContactsValidator.validateNumberOfChannels(ways)
        }
    }

    @ParameterizedTest
    @MethodSource("getIncorrectChanelTypes")
    fun `Validate incorrect number of types`(ways: ChannelsDTO, size: Int) {
        val exception = shouldThrow<ProfileException> {
            ContactsValidator.validateNumberOfChannels(ways)
        }
        exception.message shouldBe "Invalid number of communication ways. Expected 1 or 2, but was: $size"
    }

    @Test
    fun `Validate links and no links are missed`() {
        val profile = ProfileGenerator.generateProfile(PropsGenerator.generateUserId(), Instant.now())
        val channels = ChannelsDTO(phone = true, vk = true, gmail = true, chat = true)
        val links = ContactsGenerator.generateContact(profile, addVk = true, addGmail = true, addPhone = true)

        shouldNotThrow<ProfileException> {
            ContactsValidator.validateLinks(channels, links)
        }
    }

    @Test
    fun `Validate links and vk is missed`() {
        val profile = ProfileGenerator.generateProfile(PropsGenerator.generateUserId(), Instant.now())
        val channels = ChannelsDTO(vk = true)
        val links = ContactsGenerator.generateContact(profile)

        val exception = shouldThrow<ProfileException> {
            ContactsValidator.validateLinks(channels, links)
        }
        exception.message shouldBe "Can't use vk as communication channel because link is missed"
    }

    @Test
    fun `Validate links and gmail is missed`() {
        val profile = ProfileGenerator.generateProfile(PropsGenerator.generateUserId(), Instant.now())
        val channels = ChannelsDTO(gmail = true)
        val links = ContactsGenerator.generateContact(profile)

        val exception = shouldThrow<ProfileException> {
            ContactsValidator.validateLinks(channels, links)
        }
        exception.message shouldBe "Can't use gmail as communication channel because link is missed"
    }

    @Test
    fun `Validate links and phone is missed`() {
        val profile = ProfileGenerator.generateProfile(PropsGenerator.generateUserId(), Instant.now())
        val channels = ChannelsDTO(phone = true)
        val links = ContactsGenerator.generateContact(profile)

        val exception = shouldThrow<ProfileException> {
            ContactsValidator.validateLinks(channels, links)
        }
        exception.message shouldBe "Can't use phone as communication channel because link is missed"
    }
}