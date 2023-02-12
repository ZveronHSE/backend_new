package ru.zveron.authservice.persistence

import mu.KLogging
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import ru.zveron.authservice.exception.InvalidTokenException
import ru.zveron.authservice.exception.SessionExpiredException
import ru.zveron.authservice.persistence.entity.SessionEntity
import ru.zveron.authservice.persistence.repository.SessionRepository
import java.time.Instant
import java.util.UUID

@Component
@EnableConfigurationProperties(SessionProperties::class)
class SessionStorage(
    private val sessionRepository: SessionRepository,
    private val properties: SessionProperties,
) {

    companion object : KLogging()

    suspend fun createSession(fingerprint: String, profileId: Long): SessionEntity {
        val sessionEntity = SessionEntity(
            fingerprint = fingerprint,
            profileId = profileId,
            expiresAt = Instant.now().plusMillis(properties.sessionDurationMs),
        )

        return sessionRepository.save(sessionEntity)
    }

    /**
     * throws [SessionExpiredException]
     * throws [InvalidTokenException]
     * */
    @Transactional
    suspend fun updateSession(id: UUID, fingerprint: String, tokenIdentifier: UUID?): SessionEntity? {
        val sessionEntity = sessionRepository.findById(id) ?: throw SessionExpiredException()

        if (sessionEntity.fingerprint != fingerprint || sessionEntity.tokenIdentifier != tokenIdentifier) {
            throw InvalidTokenException()
        }

        if (sessionEntity.expiresAt.isBefore(Instant.now())) {
            throw SessionExpiredException()
        }

        val rowsUpdated = sessionRepository.updateSession(
            sessionId = sessionEntity.id ?: error("Session entity found but no id provided"),
            tokenIdentifier = UUID.randomUUID(),
            expiresAt = Instant.now().plusMillis(properties.sessionDurationMs)
        )

        if (rowsUpdated != 1L) {
            throw SessionExpiredException("Failed to find session in database")
        }

        return sessionRepository.findById(id)
    }

    suspend fun deleteExpired() {
        try {
            val now = Instant.now()
            val deleteRows = sessionRepository.deleteAllByExpiresAtBefore(now)
            logger.info { "Deleted $deleteRows expired sessions" }
        } catch (ex: Exception) {
            logger.error(ex) { "Failed to delete expired sessions" }
        }
    }
}

@Validated
@ConstructorBinding
@ConfigurationProperties("zveron.session")
class SessionProperties(
    val sessionDurationMs: Long,
)
