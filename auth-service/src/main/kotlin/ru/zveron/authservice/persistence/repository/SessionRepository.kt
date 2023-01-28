package ru.zveron.authservice.persistence.repository

import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.zveron.authservice.persistence.entity.SessionEntity
import java.time.Instant
import java.util.UUID

interface SessionRepository : CoroutineCrudRepository<SessionEntity, UUID> {

    @Query(
        """
        update session
        set
        token_identifier = :tokenIdentifier ,
        expires_at = :expiresAt,
        updated_at = current_timestamp,
        version = version + 1
        where id = :sessionId"""
    )
    @Modifying
    suspend fun updateSession(sessionId: UUID, tokenIdentifier: UUID, expiresAt: Instant): Long
}