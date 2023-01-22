package ru.zv.authservice.persistence.entity

import io.r2dbc.postgresql.codec.Json
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.Instant
import java.util.UUID
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Version

@Table(name = "flow_context")
data class FlowContextEntity(
    @Id
    val id: Long? = null,
    val sessionId: UUID,
    val data: Json,

    @CreatedDate
    val createdAt: Instant = Instant.now(),
    @LastModifiedDate
    val updatedAt: Instant = Instant.now(),

    @Version
    val version: Int = 0,
)
