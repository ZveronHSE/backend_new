package ru.zveron.mapper

import ru.zveron.ChannelType
import ru.zveron.Links
import ru.zveron.domain.ChannelsDTO
import ru.zveron.entity.Contact
import ru.zveron.gmail
import ru.zveron.links
import ru.zveron.phone
import ru.zveron.vKLinks

object ContactsMapper {

    fun linksModel2DTO(model: Links): ChannelsDTO =
        ChannelsDTO(
            phone = model.phone.number.isNotBlank(),
            vk = model.vk.ref.isNotBlank(),
            gmail = model.gmail.email.isNotBlank(),
            chat = true
        )

    fun linksEntity2Model(entity: Contact): Links =
        links {
            phone = phone { number = entity.phone }
            vk = vKLinks {
                ref = entity.vkRef
                email = entity.additionalEmail
            }
            gmail = gmail { email = entity.gmail }
        }

    fun channelsModel2DTO(types: Set<ChannelType>): ChannelsDTO =
        ChannelsDTO(
            phone = types.contains(ChannelType.PHONE),
            vk = types.contains(ChannelType.VK),
            gmail = types.contains(ChannelType.GOOGLE),
            chat = types.contains(ChannelType.CHAT)
        )

    fun channelsDTO2Model(dto: ChannelsDTO): List<ChannelType> =
        mutableListOf<ChannelType>().apply {
            if (dto.phone) add(ChannelType.PHONE)
            if (dto.vk) add(ChannelType.VK)
            if (dto.gmail) add(ChannelType.GOOGLE)
            if (dto.chat) add(ChannelType.CHAT)
        }
}