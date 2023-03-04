package ru.zveron.authservice.component.jwt.model

import ru.zveron.authservice.component.jwt.contant.TokenType
import java.time.Instant
import java.util.UUID

data class DecodedToken(
    val profileId: Long,
    val tokenType: TokenType,
    val expiresAt: Instant,
    val sessionId: UUID,
    val tokenIdentifier: UUID?,
) {
    fun isExpired() = this.expiresAt.isBefore(Instant.now())
}
