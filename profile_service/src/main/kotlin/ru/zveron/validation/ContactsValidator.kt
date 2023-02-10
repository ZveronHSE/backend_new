package ru.zveron.validation

import io.grpc.Status
import ru.zveron.contract.profile.model.Links
import ru.zveron.domain.channel.ChannelsDto
import ru.zveron.domain.link.LinksDto
import ru.zveron.exception.ProfileException

object ContactsValidator {

    fun validateNumberOfChannels(ways: ChannelsDto): Unit = ways.run {
        val numberOfSelectedChannels = arrayOf(phone, vk, gmail, chat).count { it }

        if (numberOfSelectedChannels !in 1..2) {
            throw ProfileException(
                "Invalid number of communication ways. Expected 1 or 2, but was: $numberOfSelectedChannels",
                Status.INVALID_ARGUMENT.code
            )
        }
    }

    fun validateLinksNotBlank(ways: ChannelsDto, linksDto: LinksDto) = ways.run {
        if (vk && linksDto.vkLink == null) {
            throw ProfileException(
                "Can't use vk as communication channel because link is missed",
                Status.INVALID_ARGUMENT.code
            )
        }
        if (gmail && linksDto.gmailLink == null) {
            throw ProfileException(
                "Can't use gmail as communication channel because link is missed",
                Status.INVALID_ARGUMENT.code
            )
        }
        if (phone && linksDto.phoneLink == null) {
            throw ProfileException(
                "Can't use phone as communication channel because link is missed",
                Status.INVALID_ARGUMENT.code
            )
        }
    }

    fun validateLinks(links: Links) {
        if ((links.vk.ref.isNotBlank() && links.vk.id.isBlank()) || (links.vk.ref.isBlank() && links.vk.id.isNotBlank())) {
            throw ProfileException("Vk id and ref should be both present or missed", Status.INVALID_ARGUMENT.code)
        }
        if ((links.gmail.email.isNotBlank() && links.gmail.id.isBlank()) || (links.gmail.email.isBlank() && links.gmail.id.isNotBlank())) {
            throw ProfileException("Gmail id and email should be both present or missed", Status.INVALID_ARGUMENT.code)
        }
    }
}