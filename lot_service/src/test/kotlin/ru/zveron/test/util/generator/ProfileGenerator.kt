package ru.zveron.test.util.generator

import com.google.protobuf.timestamp
import ru.zveron.contract.profile.getProfileWithContactsResponse
import ru.zveron.contract.profile.model.ChannelType
import ru.zveron.model.ChannelLink
import ru.zveron.model.SellerProfile
import ru.zveron.test.util.GeneratorUtils

object ProfileGenerator {
    fun generateProfileWithContacts(id: Long) = getProfileWithContactsResponse {
        this.id = id
        name = GeneratorUtils.generateString(10)
        surname = GeneratorUtils.generateString(10)
        imageUrl = GeneratorUtils.generateImageUrl()
        addressId = GeneratorUtils.generateLong()
        channels.add(ChannelType.CHAT)
        lastSeen = timestamp {
            seconds = GeneratorUtils.generateLong()
        }
    }

    fun generateSellerProfile(
        id: Long,
        isChat: Boolean = true,
        isVk: Boolean = false,
        isEmail: Boolean = false,
        isPhone: Boolean = false,
    ) =
        SellerProfile(
            id = id,
            name = GeneratorUtils.generateString(10),
            surname = GeneratorUtils.generateString(10),
            imageUrl = GeneratorUtils.generateImageUrl(),
            contact = ru.zveron.model.ChannelType(isChat = isChat, isVk = isVk, isEmail = isEmail, isPhone = isPhone),
            channelLink = ChannelLink(
                vk = if (isVk) GeneratorUtils.generateString(10) else null,
                email = if (isEmail) GeneratorUtils.generateString(10) else null,
                phone = if (isPhone) GeneratorUtils.generateString(10) else null
            ),
            isOnline = GeneratorUtils.generateBoolean()
        )
}
