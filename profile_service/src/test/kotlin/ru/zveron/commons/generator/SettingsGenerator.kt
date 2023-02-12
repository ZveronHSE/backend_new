package ru.zveron.commons.generator

import ru.zveron.domain.channel.ChannelsDto
import ru.zveron.entity.Profile
import ru.zveron.entity.Settings

object SettingsGenerator {

    fun generateSettings(
        profile: Profile,
        addVk: Boolean = false,
        addGmail: Boolean = false,
        addPhone: Boolean = false,
        addChat: Boolean = false,
        addressId: Long = -1
    ) = Settings(
        id = profile.id,
        profile = profile,
        channels = ChannelsDto(
            vk = addVk,
            gmail = addGmail,
            phone = addPhone,
            chat = addChat,
        ),
        searchAddressId = addressId
    ).also { profile.settings = it }
}