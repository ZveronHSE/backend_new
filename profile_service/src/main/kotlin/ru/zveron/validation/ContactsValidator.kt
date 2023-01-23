package ru.zveron.validation

import ru.zveron.domain.ChannelsDTO
import ru.zveron.entity.Contact
import ru.zveron.exception.ProfileException

object ContactsValidator {

    fun validateNumberOfChannels(ways: ChannelsDTO): Unit = ways.run {
        val numberOfSelectedWays = arrayOf(phone, vk, gmail, chat).count { it }

        if (numberOfSelectedWays !in 1..2) {
            throw ProfileException("Invalid number of communication ways. Expected 1 or 2, but was: $numberOfSelectedWays")
        }
    }

    fun validateLinks(ways: ChannelsDTO, links: Contact) = ways.run {
        if (vk && links.vkRef.isBlank()) {
            throw ProfileException("Can't use vk as communication channel because link is missed")
        }
        if (gmail && links.gmail.isBlank()) {
            throw ProfileException("Can't use gmail as communication channel because link is missed")
        }
        if (phone && links.phone.isBlank()) {
            throw ProfileException("Can't use phone as communication channel because link is missed")
        }
    }
}