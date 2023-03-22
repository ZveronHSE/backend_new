package ru.zveron.model.entity

import org.springframework.data.cassandra.core.cql.Ordering
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.CassandraType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import ru.zveron.model.constant.MessageType
import java.time.Instant
import java.util.*

@Table
data class Message(
    @PrimaryKeyColumn(name = "chat_id", type = PrimaryKeyType.PARTITIONED, ordinal = 1)
    val chatId: UUID,
    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 2, ordering = Ordering.DESCENDING)
    @CassandraType(type = CassandraType.Name.TIMEUUID)
    val id: UUID,
    @field:Column("received_at")
    val receivedAt: Instant,
    @field:Column("sender_id")
    val senderId: Long,
    val text: String,
    @field:Column("is_read")
    val isRead: Boolean,
    @field:Column("images_urls")
    @CassandraType(type = CassandraType.Name.LIST, typeArguments = [CassandraType.Name.TEXT])
    val imagesUrls: List<String>?,
    val type: MessageType,
)