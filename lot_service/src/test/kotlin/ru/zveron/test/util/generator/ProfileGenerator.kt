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
        imageId = GeneratorUtils.generateLong()
        addressId = GeneratorUtils.generateLong()
        channels.add(ChannelType.CHAT)
        lastSeen = timestamp {
            seconds = GeneratorUtils.generateLong()
        }
    }

    fun generateSellerProfile(id: Long) = SellerProfile(
        id = id,
        name = GeneratorUtils.generateString(10),
        surname = GeneratorUtils.generateString(10),
        imageId = GeneratorUtils.generateLong(),
        contact = ru.zveron.model.ChannelType(isChat = true),
        channelLink = ChannelLink(),
        isOnline = GeneratorUtils.generateBoolean()
    )
}