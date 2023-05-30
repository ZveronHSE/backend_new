package ru.zveron.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.cassandra.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.zveron.model.constant.ChatStatus
import ru.zveron.model.constant.MessageType
import ru.zveron.model.entity.Chat
import java.time.Instant
import java.util.UUID

interface ChatRepository : CoroutineCrudRepository<Chat, Long> {

    suspend fun findByProfileIdAndChatId(profileId: Long, chatId: UUID): Chat?
    suspend fun existsByProfileIdAndChatId(profileId: Long, chatId: UUID): Boolean

    @Query("SELECT another_profile_id FROM chat where profile_id = ?0 AND chat_id = ?1")
    suspend fun getInterlocutorId(profileId: Long, chatId: UUID): Long?

    @Query("SELECT * FROM chat WHERE profile_id = ?0 LIMIT 500000")
    fun findAllByProfileId(profileId: Long): Flow<Chat>

    @Query("SELECT * FROM chat WHERE profile_id = ?0 AND last_update < ?1 LIMIT 500000 ALLOW FILTERING")
    fun findAllByProfileIdBeforeTimestamp(profileId: Long, timestamp: Instant): Flow<Chat>

    @Query("""
        BEGIN BATCH
        UPDATE chat SET lots_ids = lots_ids + { ?0 } WHERE profile_id = ?1 AND chat_id = ?3
        UPDATE chat SET lots_ids = lots_ids + { ?0 } WHERE profile_id = ?2 AND chat_id = ?3
        APPLY BATCH
    """)
    suspend fun attachLot(lotId: Long, firstProfileId: Long, secondProfileId: Long, chatId: UUID)

    @Query("""
        BEGIN BATCH
        UPDATE chat SET lots_ids = lots_ids - { ?0 } WHERE profile_id = ?1 AND chat_id = ?3
        UPDATE chat SET lots_ids = lots_ids - { ?0 } WHERE profile_id = ?2 AND chat_id = ?3
        APPLY BATCH
    """)
    suspend fun detachLot(lotId: Long, firstProfileId: Long, secondProfileId: Long, chatId: UUID)

    @Query(
        """
        BEGIN BATCH
        INSERT INTO chat (profile_id, chat_id, last_update, another_profile_id, chat_status, unread_messages, lots_ids) VALUES (?0, ?1, ?2, ?3, ?4, 0, { ?8 });
        INSERT INTO chat (profile_id, chat_id, last_update, another_profile_id, chat_status, unread_messages, lots_ids) VALUES (?3, ?1, ?2, ?0, ?4, 1, { ?8 });
        INSERT INTO message (chat_id, id, received_at, sender_id, text, is_read, type) VALUES (?1, ?5, ?2, ?0, ?6, false, ?7);
        APPLY BATCH
    """
    )
    suspend fun crateChatsPair(
        authorizedProfileId: Long,
        chatId: UUID,
        lastUpdate: Instant,
        anotherProfileId: Long,
        status: ChatStatus,
        messageId: UUID,
        text: String,
        type: MessageType,
        lotId: Long,
    )

    @Query("UPDATE chat SET unread_messages = ?2 WHERE profile_id = ?0 AND chat_id = ?1")
    suspend fun changeUnreadMessageNumber(profileId: Long, chatId: UUID, unreadMessageNumber: Int)

    @Query("""
        BEGIN BATCH
        UPDATE chat SET last_update = ?3 WHERE profile_id = ?0 AND chat_id = ?2
        UPDATE chat SET last_update = ?3 WHERE profile_id = ?1 AND chat_id = ?2
        APPLY BATCH
    """)
    suspend fun changeLastUpdate(firstProfileId: Long, secondProfileId: Long, chatId: UUID, lastUpdate: Instant)
}