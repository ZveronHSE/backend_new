package ru.zveron.mapper

import ru.zveron.contract.chat.model.ChatFolder
import ru.zveron.contract.chat.model.Message
import ru.zveron.contract.chat.model.chat
import ru.zveron.contract.chat.model.profileSummary
import ru.zveron.contract.core.Lot
import ru.zveron.contract.profile.ProfileSummary
import ru.zveron.mapper.ProtoTypesMapper.toTimestamp
import ru.zveron.model.constant.ChatStatus
import ru.zveron.model.entity.Chat

object ChatMapper {

    fun chatToChatResponse(
        chat: Chat,
        profile: ru.zveron.contract.chat.model.ProfileSummary?,
        lots: List<Lot>?,
        messages: List<Message>?,
        isBlocked: Boolean = false,
    ): ru.zveron.contract.chat.model.Chat =
        chat {
            chatId = chat.chatId.toString()
            unreadMessages = chat.unreadMessages
            lastUpdate = chat.lastUpdate.toTimestamp()
            serviceId = chat.serviceId ?: 0L
            reviewId = chat.reviewId ?: 0L
            interlocutorSummary = profile ?: profileSummary { }
            messages?.let { this.messages.addAll(it) }
            lots?.let { this.lots.addAll(it) }
            folder = ChatStatus.DEFAULT.toFolder()
            this.isBlocked = isBlocked
        }

    fun ProfileSummary.toChatSummary(): ru.zveron.contract.chat.model.ProfileSummary =
        profileSummary {
            id = this@toChatSummary.id
            imageUrl = this@toChatSummary.imageUrl
            name = this@toChatSummary.name
            surname = this@toChatSummary.surname
            isOnline = false // TODO
            formattedOnlineStatus = "Не в сети" // TODO

        }

    fun ChatStatus.toFolder() =
        when (this) {
            ChatStatus.DEFAULT -> ChatFolder.NONE
        }
}