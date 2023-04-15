package ru.zveron.mapper

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import ru.zveron.commons.assertions.channelsShouldBe
import ru.zveron.commons.assertions.linksShouldBe
import ru.zveron.commons.generator.CommunicationLinksGenerator
import ru.zveron.commons.generator.ProfileGenerator
import ru.zveron.commons.generator.PropsGenerator
import ru.zveron.contract.profile.model.ChannelType
import ru.zveron.contract.profile.model.gmail
import ru.zveron.contract.profile.model.links
import ru.zveron.contract.profile.model.mailru
import ru.zveron.contract.profile.model.phone
import ru.zveron.contract.profile.model.vk
import ru.zveron.domain.channel.ChannelsDto
import ru.zveron.domain.link.GmailData
import ru.zveron.domain.link.MailRuData
import ru.zveron.domain.link.PhoneData
import ru.zveron.domain.link.VkData
import ru.zveron.entity.CommunicationLink
import ru.zveron.mapper.ContactsMapper.toDto
import ru.zveron.mapper.ContactsMapper.toLinks
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
            mail = mailru {
                id = PropsGenerator.generateString(10)
                email = PropsGenerator.generateString(10)
            }
        }

        val actual = expected.toDto()

        actual.chat shouldBe true
        actual.vk shouldBe true
        actual.gmail shouldBe true
        actual.phone shouldBe true
        actual.mailRu shouldBe true
    }

    @Test
    fun `linksList2Dto maps correctly`() {
        val profile = ProfileGenerator.generateProfile(Instant.now())
        val vk = CommunicationLink(
            communicationLinkId = PropsGenerator.generateString(10),
            data = VkData(
                ref = PropsGenerator.generateString(10),
                email = ""
            ),
            profile = profile
        )
        val gmail = CommunicationLink(
            communicationLinkId = PropsGenerator.generateString(10),
            data = GmailData(
                email = PropsGenerator.generateString(10),
            ),
            profile = profile
        )
        val mailRu = CommunicationLink(
            communicationLinkId = PropsGenerator.generateString(10),
            data = MailRuData(
                email = PropsGenerator.generateString(10),
            ),
            profile = profile
        )
        val phone = CommunicationLink(
            communicationLinkId = PropsGenerator.generateString(10),
            data = PhoneData(),
            profile = profile
        )
        val list = listOf(vk, gmail, phone, mailRu)

        val dto = list.toDto()

        dto.vkLink shouldBe vk
        dto.gmailLink shouldBe gmail
        dto.phoneLink shouldBe phone
        dto.mailRuLink shouldBe mailRu
    }

    @Test
    fun `linksDto2Model maps correctly`() {
        val profile = ProfileGenerator.generateProfile(Instant.now())
        val expected =
            CommunicationLinksGenerator.generateLinks(profile, addVk = true, addGmail = true, addPhone = true, addMailRu = true)

        val actual = expected.toLinks()

        actual linksShouldBe expected
    }

    @Test
    fun `channelsModel2DTO maps correctly`() {
        val set = setOf(ChannelType.PHONE, ChannelType.CHAT, ChannelType.VK, ChannelType.GOOGLE, ChannelType.MAILRU)

        val actual = set.toDto()

        actual.apply {
            vk shouldBe true
            gmail shouldBe true
            phone shouldBe true
            chat shouldBe true
            mailRu shouldBe true
        }
    }

    @Test
    fun `channelsDTO2Model maps correctly`() {
        val expected = ChannelsDto(phone = true, vk = true, gmail = true, chat = true, mailRu = true)

        val actual = expected.toModel()

        actual channelsShouldBe expected
    }
}