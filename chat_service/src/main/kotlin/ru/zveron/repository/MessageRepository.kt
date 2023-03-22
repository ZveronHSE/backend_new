package ru.zveron.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.cassandra.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.zveron.model.entity.Message
import java.util.UUID

interface MessageRepository : CoroutineCrudRepository<Message, String> {

    @Query("SELECT * FROM message WHERE chat_id = ?0 AND id = ?1")
    suspend fun findExact(chatId: UUID, messageId: UUID): Message?

    @Query("SELECT * FROM message WHERE chat_id = ?0 LIMIT ?1")
    fun getChatRecentMessages(chatId: UUID, pageSize: Int): Flow<Message>

    @Query("SELECT * FROM message WHERE chat_id = ?0 AND id < ?1 LIMIT ?2")
    fun getChatRecentMessages(chatId: UUID, before: UUID, pageSize: Int): Flow<Message>
}