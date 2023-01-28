package ru.zveron.authservice.component.jwt.model

import com.fasterxml.jackson.annotation.JsonCreator
import java.time.Instant
import java.util.UUID

data class DecodedToken(
    val profileId: Long,
    val tokenType: TokenType,
    val issuer: String,
    val expiresAt: Instant,
    val sessionId: UUID,
    val tokenIdentifier: UUID,
)

enum class TokenType {
    ACCESS,
    REFRESH,
    ;

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromName(name: String) =
            values().singleOrNull { it.name.equals(name, true) } ?: error("Unknown token type provided $name")
    }
}