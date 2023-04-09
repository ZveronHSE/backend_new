package ru.zveron.mapper

import ru.zveron.contract.profile.model.ChannelType
import ru.zveron.contract.profile.model.Links
import ru.zveron.domain.channel.ChannelsDto
import ru.zveron.contract.profile.model.gmail
import ru.zveron.contract.profile.model.links
import ru.zveron.contract.profile.model.mailru
import ru.zveron.contract.profile.model.phone
import ru.zveron.contract.profile.model.vk
import ru.zveron.domain.link.*
import ru.zveron.entity.CommunicationLink
import ru.zveron.entity.Profile

object ContactsMapper {

    fun Links.toDto(): ChannelsDto =
        ChannelsDto(
            phone = phone.number.isNotBlank(),
            vk = vk.ref.isNotBlank(),
            gmail = gmail.email.isNotBlank(),
            mailRu = mail.email.isNotBlank(),
            chat = true
        )

    fun Links.toCommunicationLinks(profile: Profile): List<CommunicationLink> {
        val result = mutableListOf<CommunicationLink>()
        if (vk.id.isNotBlank()) {
            result.add(
                CommunicationLink(
                    communicationLinkId = vk.id,
                    data = VkData(
                        ref = vk.ref,
                        email = vk.email,
                    ),
                    profile = profile
                )
            )
        }
        if (gmail.id.isNotBlank()) {
            result.add(
                CommunicationLink(
                    communicationLinkId = gmail.id,
                    data = GmailData(
                        email = gmail.email,
                    ),
                    profile = profile
                )
            )
        }
        if (phone.number.isNotBlank()) {
            result.add(
                CommunicationLink(
                    communicationLinkId = phone.number,
                    data = PhoneData(),
                    profile = profile
                )
            )
        }
        if (mail.id.isNotBlank()) {
            result.add(
                CommunicationLink(
                    communicationLinkId = mail.id,
                    data = MailRuData(
                        email = mail.email,
                    ),
                    profile = profile
                )
            )
        }
        return result
    }

    fun List<CommunicationLink>.toDto(): LinksDto {
        val map = this.associateBy { it.data.type }

        return LinksDto(
            phoneLink = map[CommunicationLinkType.PHONE],
            vkLink = map[CommunicationLinkType.VK],
            gmailLink = map[CommunicationLinkType.GMAIL],
            mailRuLink = map[CommunicationLinkType.MAIL_RU],
        )
    }

    fun LinksDto.toLinks(): Links =
        links {
            phoneLink?.apply {
                phone = phone { number = this@apply.communicationLinkId }
            }
            gmailLink?.apply {
                gmail = gmail {
                    id = this@apply.communicationLinkId
                    email = (this@apply.data as GmailData).email
                }
            }
            vkLink?.apply {
                vk = vk {
                    id = this@apply.communicationLinkId
                    ref = (this@apply.data as VkData).ref
                    email = this@apply.data.email
                }
            }
            mailRuLink?.apply {
                mail = mailru {
                    id = this@apply.communicationLinkId
                    email = (this@apply.data as MailRuData).email
                }
            }
        }

    fun Set<ChannelType>.toDto(): ChannelsDto =
        ChannelsDto(
            phone = contains(ChannelType.PHONE),
            vk = contains(ChannelType.VK),
            gmail = contains(ChannelType.GOOGLE),
            mailRu = contains(ChannelType.MAILRU),
            chat = contains(ChannelType.CHAT)
        )

    fun ChannelsDto.toModel(): List<ChannelType> =
        mutableListOf<ChannelType>().apply {
            if (phone) add(ChannelType.PHONE)
            if (vk) add(ChannelType.VK)
            if (gmail) add(ChannelType.GOOGLE)
            if (mailRu) add(ChannelType.MAILRU)
            if (chat) add(ChannelType.CHAT)
        }
}