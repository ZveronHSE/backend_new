package ru.zveron.service.domain

import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service
import ru.zveron.contract.chat.model.MessagePagination
import ru.zveron.model.constant.MessageType
import ru.zveron.model.entity.Message
import ru.zveron.repository.MessageRepository
import java.time.Instant
import java.util.*

private const val DEFAULT_MESSAGES_PAGINATION_PAGE_SIZE = 100

@Service
class MessageDomainService(private val repository: MessageRepository) {

    fun getChatRecentMessages(
        chatId: UUID,
        pagination: MessagePagination? = null
    ): Flow<Message> {
        if (pagination != null && pagination.hasSize() && pagination.messagesBeforeId.isNotEmpty()) {
            return repository.getChatRecentMessages(
                chatId,
                UUID.fromString(pagination.messagesBeforeId),
                pagination.size
            )
        }

        return repository.getChatRecentMessages(chatId, DEFAULT_MESSAGES_PAGINATION_PAGE_SIZE)
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