package ru.zveron.mapper

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import ru.zveron.commons.assertions.channelsShouldBe
import ru.zveron.commons.assertions.linksShouldBe
import ru.zveron.commons.generator.ContactsGenerator
import ru.zveron.commons.generator.ProfileGenerator
import ru.zveron.commons.generator.PropsGenerator
import ru.zveron.contract.profile.model.ChannelType
import ru.zveron.contract.profile.model.gmail
import ru.zveron.contract.profile.model.links
import ru.zveron.contract.profile.model.phone
import ru.zveron.contract.profile.model.vk
import ru.zveron.domain.ChannelsDto
import ru.zveron.mapper.ContactsMapper.toDto
import ru.zveron.mapper.ContactsMapper.toModel
import java.time.Instant

class ContactsMapperTest {

    @Test
    fun `linksModel2DTO maps correctly`() {
        val expected = links {
            phone = phone { number = PropsGenerator.generateString(10) }
            vk = vk {
                id = PropsGenerator.generateString(10)
                ref = PropsGenerator.generateString(10)
                email = PropsGenerator.generateString(10)
            }
            gmail = gmail {
                id = PropsGenerator.generateString(10)
                email = PropsGenerator.generateString(10)
            }
        }

        val actual = expected.toDto()

        actual.chat shouldBe true
        actual.vk shouldBe true
        actual.gmail shouldBe true
        actual.phone shouldBe true
    }

    @Test
    fun `linksEntity2Model maps correctly`() {
        val profile = ProfileGenerator.generateProfile(PropsGenerator.generateUserId(), Instant.now())
        val expected = ContactsGenerator.generateContact(profile, addVk = true, addGmail = true, addPhone = true)

        val actual = expected.toModel()

        actual linksShouldBe expected
    }

    @Test
    fun `channelsModel2DTO maps correctly`() {
        val set = setOf(ChannelType.PHONE, ChannelType.CHAT, ChannelType.VK, ChannelType.GOOGLE)

        val actual = set.toDto()

        actual.apply {
            vk shouldBe true
            gmail shouldBe true
            phone shouldBe true
            chat shouldBe true
        }
    }

    @Test
    fun `channelsDTO2Model maps correctly`() {
        val expected = ChannelsDto(phone = true, vk = true, gmail = true, chat = true)

        val actual = expected.toModel()

        actual channelsShouldBe expected
    }
}