package ru.zveron.domain.channel

import java.io.Serializable

data class ChannelsDto(
    var phone: Boolean = false,
    var vk: Boolean = false,
    var gmail: Boolean = false,
    var mailRu: Boolean = false,
    var chat: Boolean = false,
) : Serializable