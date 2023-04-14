package ru.zveron.validation

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import ru.zveron.commons.generator.CommunicationLinksGenerator
import ru.zveron.commons.generator.ProfileGenerator
import ru.zveron.commons.generator.PropsGenerator
import ru.zveron.domain.channel.ChannelsDto
import ru.zveron.exception.ProfileException
import java.time.Instant

class ContactsValidatorTest {

    companion object {
        @JvmStatic
        private fun getCorrectChanelTypes() = listOf(
            Arguments.of(ChannelsDto(phone = true)),
            Arguments.of(ChannelsDto(vk = true, gmail = true)),
            Arguments.of(ChannelsDto(mailRu = true)),
        )

        @JvmStatic
        private fun getIncorrectChanelTypes() = listOf(
            Arguments.of(ChannelsDto(), 0),
            Arguments.of(ChannelsDto(phone = true, vk = true, gmail = true), 3),
            Arguments.of(ChannelsDto(phone = true, vk = true, gmail = true, chat = true), 4),
            Arguments.of(ChannelsDto(phone = true, vk = true, gmail = true, chat = true, mailRu = true), 5),
        )
    }

    @ParameterizedTest
    @MethodSource("getCorrectChanelTypes")
    fun `Validate correct number of types`(ways: ChannelsDto) {
        shouldNotThrow<ProfileException> {
            ContactsValidator.validateNumberOfChannels(ways)
        }
    }

    @ParameterizedTest
    @MethodSource("getIncorrectChanelTypes")
    fun `Validate incorrect number of types`(ways: ChannelsDto, size: Int) {
        val exception = shouldThrow<ProfileException> {
            ContactsValidator.validateNumberOfChannels(ways)
        }
        exception.message shouldBe "Invalid number of communication ways. Expected 1 or 2, but was: $size"
    }

    @Test
    fun `Validate links and no links are missed`() {
        val profile = ProfileGenerator.generateProfile(Instant.now())
        val channels = ChannelsDto(phone = true, vk = true, gmail = true, chat = true)
        val links = CommunicationLinksGenerator.generateLinks(profile, addVk = true, addGmail = true, addPhone = true)

        shouldNotThrow<ProfileException> {
            ContactsValidator.validateLinksNotBlank(channels, links)
        }
    }

    @Test
    fun `Validate links and vk is missed`() {
        val profile = ProfileGenerator.generateProfile(Instant.now())
        val channels = ChannelsDto(vk = true)
        val links = CommunicationLinksGenerator.generateLinks(profile)

        val exception = shouldThrow<ProfileException> {
            ContactsValidator.validateLinksNotBlank(channels, links)
        }
        exception.message shouldBe "Can't use vk as communication channel because link is missed"
    }

    @Test
    fun `Validate links and gmail is missed`() {
        val profile = ProfileGenerator.generateProfile(Instant.now())
        val channels = ChannelsDto(gmail = true)
        val links = CommunicationLinksGenerator.generateLinks(profile)

        val exception = shouldThrow<ProfileException> {
            ContactsValidator.validateLinksNotBlank(channels, links)
        }
        exception.message shouldBe "Can't use gmail as communication channel because link is missed"
    }

    @Test
    fun `Validate links and mailRu is missed`() {
        val profile = ProfileGenerator.generateProfile(Instant.now())
        val channels = ChannelsDto(mailRu = true)
        val links = CommunicationLinksGenerator.generateLinks(profile)

        val exception = shouldThrow<ProfileException> {
            ContactsValidator.validateLinksNotBlank(channels, links)
        }
        exception.message shouldBe "Can't use mail.ru as communication channel because link is missed"
    }

    @Test
    fun `Validate links and phone is missed`() {
        val profile = ProfileGenerator.generateProfile(Instant.now())
        val channels = ChannelsDto(phone = true)
        val links = CommunicationLinksGenerator.generateLinks(profile)

        val exception = shouldThrow<ProfileException> {
            ContactsValidator.validateLinksNotBlank(channels, links)
        }
        exception.message shouldBe "Can't use phone as communication channel because link is missed"
    }

    @Test
    fun `Validate links and vk id is missed`() {
        val links = CommunicationLinksGenerator.generateLinks(vkRef = PropsGenerator.generateString(10))

        val exception = shouldThrow<ProfileException> {
            ContactsValidator.validateLinks(links)
        }
        exception.message shouldBe "Vk id and ref should be both present or missed"
    }

    @Test
    fun `Validate links and vk ref is missed`() {
        val links = CommunicationLinksGenerator.generateLinks(vkId = PropsGenerator.generateString(10))

        val exception = shouldThrow<ProfileException> {
            ContactsValidator.validateLinks(links)
        }
        exception.message shouldBe "Vk id and ref should be both present or missed"
    }

    @Test
    fun `Validate links and gmail id is missed`() {
        val links = CommunicationLinksGenerator.generateLinks(gmail = PropsGenerator.generateString(10))

        val exception = shouldThrow<ProfileException> {
            ContactsValidator.validateLinks(links)
        }
        exception.message shouldBe "Gmail id and email should be both present or missed"
    }

    @Test
    fun `Validate links and gmail email is missed`() {
        val links = CommunicationLinksGenerator.generateLinks(gmailId = PropsGenerator.generateString(10))

        val exception = shouldThrow<ProfileException> {
            ContactsValidator.validateLinks(links)
        }
        exception.message shouldBe "Gmail id and email should be both present or missed"
    }

    @Test
    fun `Validate links and mailRu id is missed`() {
        val links = CommunicationLinksGenerator.generateLinks(mailRuEmail = PropsGenerator.generateString(10))

        val exception = shouldThrow<ProfileException> {
            ContactsValidator.validateLinks(links)
        }
        exception.message shouldBe "Mail.ru id and email should be both present or missed"
    }

    @Test
    fun `Validate links and mailRu email is missed`() {
        val links = CommunicationLinksGenerator.generateLinks(mailRuId = PropsGenerator.generateString(10))

        val exception = shouldThrow<ProfileException> {
            ContactsValidator.validateLinks(links)
        }
        exception.message shouldBe "Mail.ru id and email should be both present or missed"
    }
}