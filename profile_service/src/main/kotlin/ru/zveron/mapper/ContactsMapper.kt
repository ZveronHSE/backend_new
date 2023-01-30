package ru.zveron.mapper

import ru.zveron.contract.profile.model.ChannelType
import ru.zveron.contract.profile.model.Links
import ru.zveron.contract.profile.model.gmail
import ru.zveron.contract.profile.model.links
import ru.zveron.contract.profile.model.phone
import ru.zveron.contract.profile.model.vk
import ru.zveron.domain.ChannelsDto
import ru.zveron.entity.Contact

object ContactsMapper {

    fun Links.toDto(): ChannelsDto =
        ChannelsDto(
            phone = phone.number.isNotBlank(),
            vk = vk.ref.isNotBlank(),
            gmail = gmail.email.isNotBlank(),
            chat = true
        )

    fun Contact.toModel(): Links =
        links {
            phone = phone { number = this@toModel.phone }
            vk = vk {
                id = this@toModel.vkId
                ref = this@toModel.vkRef
                email = this@toModel.additionalEmail
            }
            gmail = gmail {
                id = this@toModel.gmailId
                email = this@toModel.gmail
            }
        }

    fun Set<ChannelType>.toDto(): ChannelsDto =
        ChannelsDto(
            phone = contains(ChannelType.PHONE),
            vk = contains(ChannelType.VK),
            gmail = contains(ChannelType.GOOGLE),
            chat = contains(ChannelType.CHAT)
        )

    fun ChannelsDto.toModel(): List<ChannelType> =
        mutableListOf<ChannelType>().apply {
            if (phone) add(ChannelType.PHONE)
            if (vk) add(ChannelType.VK)
            if (gmail) add(ChannelType.GOOGLE)
            if (chat) add(ChannelType.CHAT)
        }
}
