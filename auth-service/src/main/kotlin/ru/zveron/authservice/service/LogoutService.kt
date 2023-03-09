package ru.zveron.authservice.service

import mu.KLogging
import org.springframework.stereotype.Service
import ru.zveron.authservice.component.jwt.JwtManager
import ru.zveron.authservice.persistence.repository.SessionRepository

@Service
class LogoutService(
    private val sessionRepository: SessionRepository,
    private val jwtManager: JwtManager,
) {

    companion object : KLogging()

    /**
     * @throws [InvalidTokenException]
     */
    suspend fun logout(accessToken: String) {
        val decodeAccessToken = jwtManager.decodeAccessToken(accessToken)

        if (decodeAccessToken.isExpired()) {
            logger.error { "Access token is expired, when trying to logout" }
        }

        sessionRepository.deleteAllById(setOf(decodeAccessToken.sessionId))
    }
}
