package ru.zveron.model

import java.io.Serializable

data class ChannelType(
    var isPhone: Boolean = false,
    var isVk: Boolean = false,
    var isEmail: Boolean = false,
    var isChat: Boolean = false
) : Serializable