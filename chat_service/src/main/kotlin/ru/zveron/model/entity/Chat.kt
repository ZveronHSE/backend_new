package ru.zveron.model.entity

import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.CassandraType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import ru.zveron.model.enum.ChatStatus
import ru.zveron.model.enum.FolderType
import java.time.Instant

@Table
data class Chat(
    @PrimaryKeyColumn(name = "user_id", type = PrimaryKeyType.PARTITIONED, ordinal = 1)
    val userId: Long,
    @PrimaryKeyColumn(name = "folder_type", type = PrimaryKeyType.PARTITIONED, ordinal = 2)
    @CassandraType(type = CassandraType.Name.TEXT)
    val folderType: FolderType,
    @PrimaryKeyColumn(name = "chat_id", type = PrimaryKeyType.CLUSTERED, ordinal = 3)
    val chatId: String,
    @field:Column("last_update")
    val lastUpdate: Instant,
    @field:Column("another_user_id")
    val anotherUserId: Long,
    @field:Column("lots_ids")
    val lotsIds: List<Long>?,
    @field:Column("service_id")
    val serviceId: Long?,
    @field:Column("unread_messages")
    val unreadMessages: Int,
    @CassandraType(type = CassandraType.Name.TEXT)
    val status: ChatStatus,
    @field:Column("review_id")
    val reviewId: Long?,
)