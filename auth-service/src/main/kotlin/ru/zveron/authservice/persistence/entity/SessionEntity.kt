package ru.zveron.authservice.persistence.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("session")
data class SessionEntity(

    @Id
    val id: UUID? = null,

    //key that matches session to refresh token
    val tokenIdentifier: UUID = UUID.randomUUID(),

    //user device fingerprint (unique for each device)
    val fingerprint: String,

    val profileId: Long,

    val expiresAt: Instant,

    @CreatedDate
    val createdAt: Instant = Instant.now(),

    @LastModifiedDate
    val updatedAt: Instant = Instant.now(),

    @Version
    val version: Int = 0,
)
