package ru.zveron.model.entity

import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.CassandraType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import ru.zveron.model.constant.ChatStatus
import java.time.Instant
import java.util.UUID

@Table
data class Chat(
    @PrimaryKeyColumn(name = "profile_id", type = PrimaryKeyType.PARTITIONED, ordinal = 1)
    val profileId: Long,
    @PrimaryKeyColumn(name = "chat_id", type = PrimaryKeyType.CLUSTERED, ordinal = 2)
    val chatId: UUID,
    @field:Column("last_update")
    val lastUpdate: Instant,
    @field:Column("another_profile_id")
    val anotherProfileId: Long,
    @field:Column("lots_ids")
    /**
     * Связанные с чатом объявления
     */
    val lotsIds: Set<Long>?,
    @field:Column("service_id")
    /**
     * Связанная с чатом услуга
     */
    val serviceId: Long?,
    @field:Column("unread_messages")
    val unreadMessages: Int,
    @field:Column("chat_status")
    @CassandraType(type = CassandraType.Name.TEXT)
    val chatStatus: ChatStatus,
    @field:Column("review_id")
    val reviewId: Long?,
)