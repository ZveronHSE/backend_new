package ru.zveron.model

import com.fasterxml.jackson.annotation.JsonAlias
import java.io.Serializable

data class ChannelType(
    @field:JsonAlias("phone")
    var isPhone: Boolean = false,
    @field:JsonAlias("vk")
    var isVk: Boolean = false,
    @field:JsonAlias("email")
    var isEmail: Boolean = false,
    @field:JsonAlias("chat")
    var isChat: Boolean = false
) : Serializable