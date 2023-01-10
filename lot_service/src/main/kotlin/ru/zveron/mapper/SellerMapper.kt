package ru.zveron.mapper

import ru.zveron.contract.lot.model.CommunicationChannel
import ru.zveron.contract.profile.GetProfileWithContactsResponse
import ru.zveron.contract.profile.model.ChannelType
import ru.zveron.contract.profile.model.Links
import ru.zveron.model.ChannelLink
import ru.zveron.model.SellerProfile

object SellerMapper {

    private const val MAILTO_LINK = "mailto:"
    private const val TEL_LINK = "tel:"

    fun GetProfileWithContactsResponse.toSellerProfile() = SellerProfile(
        id = id,
        name = name,
        surname = surname,
        imageId = imageId,
        contact = channelsList.toContact(),
        isOnline = lastSeen.isInitialized,
        channelLink = links.toChannelLink()
    )

    private fun Links.toChannelLink(): ChannelLink {
        return ChannelLink(
            vk = if (vk.isInitialized) vk.ref else null,
            email = if (gmail.isInitialized) "$MAILTO_LINK${gmail.email}" else null,
            phone = if (phone.isInitialized) "$TEL_LINK${phone.number}" else null
        )
    }

    private fun List<ChannelType>.toContact(): ru.zveron.model.ChannelType {
        val channelType = ru.zveron.model.ChannelType()

        for (channel in this) {
            when (channel) {
                ChannelType.VK -> channelType.vk = true
                ChannelType.CHAT -> channelType.chat = true
                ChannelType.GOOGLE -> channelType.email = true
                ChannelType.PHONE -> channelType.phone = true
                else -> {}
            }
        }

        return channelType
    }

    fun List<CommunicationChannel>.toChannelType(): ru.zveron.model.ChannelType {
        val channelType = ru.zveron.model.ChannelType()

        for (channel in this) {
            when (channel) {
                CommunicationChannel.VK -> channelType.vk = true
                CommunicationChannel.CHAT -> channelType.chat = true
                CommunicationChannel.EMAIL -> channelType.email = true
                CommunicationChannel.PHONE -> channelType.phone = true
                else -> {}
            }
        }

        return channelType
    }

    fun ru.zveron.model.ChannelType.toCommunicationChannels(): List<CommunicationChannel> {
        val communicationChannels = mutableListOf<CommunicationChannel>()

        if (vk) {
            communicationChannels.add(CommunicationChannel.VK)
        }

        if (chat) {
            communicationChannels.add(CommunicationChannel.CHAT)
        }

        if (email) {
            communicationChannels.add(CommunicationChannel.EMAIL)
        }

        if (phone) {
            communicationChannels.add(CommunicationChannel.PHONE)
        }

        return communicationChannels
    }
}