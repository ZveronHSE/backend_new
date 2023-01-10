package ru.zveron.model

data class SellerProfile(
    val id: Long,
    val name: String,
    val surname: String,
    val imageId: Long,
    val contact: ChannelType,
    val channelLink: ChannelLink,
    val isOnline: Boolean
)