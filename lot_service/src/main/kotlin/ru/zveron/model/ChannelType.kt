package ru.zveron.model

import java.io.Serializable

data class ChannelType(
    var phone: Boolean = false,
    var vk: Boolean = false,
    var email: Boolean = false,
    var chat: Boolean = false
) : Serializable