package ru.zveron.component

import kotlinx.coroutines.flow.Flow
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.zveron.contract.chat.model.MessagePagination
import ru.zveron.model.constant.MessageType
import ru.zveron.model.entity.Message
import ru.zveron.repository.MessageRepository
import java.time.Instant
import java.util.*

@Component
class MessageStorage(private val repository: MessageRepository) {

    @Value("\${service.persistence.message.pagination-size}")
    var paginationSize: Int = 0

    fun getChatRecentMessages(
        chatId: UUID,
        pagination: MessagePagination? = null
    ): Flow<Message> {
        val limit = pagination?.takeIf { it.hasSize() }?.size ?: paginationSize
        if (pagination != null && pagination.messagesBeforeId.isNotEmpty()) {
            return repository.getChatMessagesBefore(
                chatId,
                UUID.fromString(pagination.messagesBeforeId),
                limit
            )
        }

        return repository.getChatRecentMessages(chatId, limit)
    }

    suspend fun saveMessage(chatId: UUID, messageId: UUID, senderId: Long, text: String, images: List<String>) =
        repository.save(
            Message(
                chatId,
                messageId,
                Instant.now(),
                senderId,
                text,
                false,
                images,
                MessageType.DEFAULT,
            )
        )
}