package ru.zveron.common.generator

import com.datastax.oss.driver.api.core.uuid.Uuids
import ru.zveron.contract.chat.model.Message
import ru.zveron.contract.chat.model.ProfileSummary
import ru.zveron.contract.chat.model.chat
import ru.zveron.contract.core.Lot
import ru.zveron.mapper.ProtoTypesMapper.toTimestamp
import ru.zveron.model.entity.Chat
import ru.zveron.model.constant.ChatStatus
import java.time.Instant

object ChatGenerator {

    fun generateChat(
        userId: Long,
        anotherUserId: Long,
        lastUpdate: Instant = Instant.now(),
        lotsIds: Set<Long>? = null,
        serviceId: Long? = null,
        reviewId: Long? = null,
    ) = Chat(
        profileId = userId,
        chatId = Uuids.timeBased(),
        lastUpdate = lastUpdate,
        anotherProfileId = anotherUserId,
        lotsIds = lotsIds,
        serviceId = serviceId,
        unreadMessages = 0,
        chatStatus = ChatStatus.DEFAULT,
        reviewId = reviewId,
    )

    fun generateChatResponse(
        chat: Chat,
        profileSummary: ProfileSummary,
        lots: List<Lot>,
        messages: List<Message>,
        isBlocked: Boolean = false
    ) = chat {
        chatId = chat.chatId.toString()
        unreadMessages = chat.unreadMessages
        serviceId = chat.serviceId ?: 0L
        reviewId = chat.reviewId ?: 0L
        lastUpdate = chat.lastUpdate.toTimestamp()

        interlocutorSummary = profileSummary
        this.lots.addAll(lots)
        this.messages.addAll(messages)

        this.isBlocked = isBlocked
    }
}