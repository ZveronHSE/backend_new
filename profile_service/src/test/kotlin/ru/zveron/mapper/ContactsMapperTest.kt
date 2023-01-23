package ru.zveron.mapper

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import ru.zveron.ChannelType
import ru.zveron.commons.assertions.channelsShouldBe
import ru.zveron.commons.assertions.linksShouldBe
import ru.zveron.commons.generator.ContactsGenerator
import ru.zveron.commons.generator.ProfileGenerator
import ru.zveron.commons.generator.PropsGenerator
import ru.zveron.domain.ChannelsDTO
import ru.zveron.gmail
import ru.zveron.links
import ru.zveron.phone
import ru.zveron.vKLinks
import java.time.Instant

class ContactsMapperTest {

    @Test
    fun `linksModel2DTO maps correctly`() {
        val expected = links {
            phone = phone { number = PropsGenerator.generateString(10) }
            vk = vKLinks {
                ref = PropsGenerator.generateString(10)
                email = PropsGenerator.generateString(10)
            }
            gmail = gmail {
                email = PropsGenerator.generateString(10)
            }
        }

        val actual = ContactsMapper.linksModel2DTO(expected)

        actual.chat shouldBe true
        actual.vk shouldBe true
        actual.gmail shouldBe true
        actual.phone shouldBe true
    }

    @Test
    fun `linksEntity2Model maps correctly`() {
        val profile = ProfileGenerator.generateProfile(PropsGenerator.generateUserId(), Instant.now())
        val expected = ContactsGenerator.generateContact(profile, addVk = true, addGmail = true, addPhone = true)

        val actual = ContactsMapper.linksEntity2Model(expected)

        actual linksShouldBe expected
    }

    @Test
    fun `channelsModel2DTO maps correctly`() {
        val set = setOf(ChannelType.PHONE, ChannelType.CHAT, ChannelType.VK, ChannelType.GOOGLE)

        val actual = ContactsMapper.channelsModel2DTO(set)

        actual.apply {
            vk shouldBe true
            gmail shouldBe true
            phone shouldBe true
            chat shouldBe true
        }
    }

    @Test
    fun `channelsDTO2Model maps correctly`() {
        val expected = ChannelsDTO(phone = true, vk = true, gmail = true, chat = true)

        val actual = ContactsMapper.channelsDTO2Model(expected)

        actual channelsShouldBe expected
    }
}