package ru.zveron.component

import kotlinx.coroutines.flow.toList
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.zveron.contract.chat.model.ChatPagination
import ru.zveron.exception.InvalidParamChatException
import ru.zveron.mapper.ProtoTypesMapper.toInstant
import ru.zveron.model.constant.ChatStatus
import ru.zveron.model.constant.MessageType
import ru.zveron.model.entity.Chat
import ru.zveron.repository.ChatRepository
import java.time.Instant
import java.util.UUID
import kotlin.math.max

@Component
class ChatStorage(private val repository: ChatRepository) {

    @Value("\${service.persistence.chat.pagination-size}")
    var paginationSize: Int = 0

    suspend fun findExact(profileId: Long, chatId: UUID) = repository.findByProfileIdAndChatId(profileId, chatId)

    suspend fun chatExists(profileId: Long, chatId: UUID) = repository.existsByProfileIdAndChatId(profileId, chatId)

    suspend fun getRecentChats(
        profileId: Long,
        pagination: ChatPagination,
    ): List<Chat> {
        val limit = pagination.takeIf { it.hasSize() }?.size ?: paginationSize
        val chats = if (pagination.hasTimeBefore()) {
            repository.findAllByProfileIdBeforeTimestamp(profileId, pagination.timeBefore.toInstant())
        } else {
            repository.findAllByProfileId(profileId)
        }

        return chats.toList().sortedByDescending { it.lastUpdate }.take(limit)
    }

    suspend fun getInterlocutorId(profileId: Long, chatId: UUID) = repository.getInterlocutorId(profileId, chatId)

    suspend fun attachLotToChat(lotId: Long, firstUserId: Long, secondUSerId: Long, chatId: UUID) =
        repository.attachLot(lotId, firstUserId, secondUSerId, chatId)

    suspend fun detachLot(lotId: Long, firstProfileId: Long, secondProfileId: Long, chatId: UUID) =
        repository.detachLot(lotId, firstProfileId, secondProfileId, chatId)

    suspend fun createChatsPair(
        authorizedProfileId: Long,
        anotherProfileId: Long,
        chatId: UUID,
        lotId: Long,
        messageId: UUID,
        text: String,
        receivedAt: Instant,
    ) =
        repository.crateChatsPair(
            authorizedProfileId,
            chatId,
            receivedAt,
            anotherProfileId,
            ChatStatus.DEFAULT,
            messageId,
            text,
            MessageType.DEFAULT,
            lotId
        )

    suspend fun incrementUnreadCounter(profileId: Long, chatId: UUID, incrementSize: Int) {
        // TODO переделывать
        val chat = findExact(profileId, chatId) ?: throw InvalidParamChatException("Chat with id $chatId doe not exists for profile $profileId")
        repository.changeUnreadMessageNumber(profileId, chatId, chat.unreadMessages + incrementSize)
    }

    suspend fun decrementUnreadCounter(profileId: Long, chatId: UUID, decrementSize: Int) {
        // TODO переделывать
        val chat = findExact(profileId, chatId) ?: throw InvalidParamChatException("Chat with id $chatId doe not exists for profile $profileId")
        // TODO еще больший кринж чем все остальное, надо смотреть сколько сообщений реально прочиталось
        repository.changeUnreadMessageNumber(profileId, chatId, max(chat.unreadMessages - decrementSize, 0))
    }

    suspend fun changeLastUpdate(profileId1: Long, profileId2: Long, chatId: UUID, timestamp: Instant) =
        repository.changeLastUpdate(profileId1, profileId2, chatId, timestamp)
}