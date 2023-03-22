package ru.zveron.model.entity

import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.util.UUID

@Table
data class Connection(
    @PrimaryKeyColumn(name = "user_id", type = PrimaryKeyType.PARTITIONED, ordinal = 1)
    val userId: Long,
    @PrimaryKeyColumn(name = "node_address", type = PrimaryKeyType.PARTITIONED, ordinal = 2)
    val nodeAddress: UUID,
    @field:Column("is_closed")
    val isClosed: Boolean,
)