package ru.zv.authservice.service.dto

import ru.zv.authservice.component.jwt.JwtMapper.toServiceResponse
import ru.zv.authservice.component.jwt.MobileTokens
import java.time.Instant
import java.util.UUID

data class LoginByPhoneVerifyResponse(
    val sessionId: UUID,
    val isNewUser: Boolean = false,
    val tokens: JwtMobileTokens,
) {
    companion object {
        fun registration(sessionId: UUID) = LoginByPhoneVerifyResponse(
            sessionId = sessionId,
            isNewUser = true,
            tokens = JwtMobileTokens(
                accessToken = "",
                refreshToken = "",
            )
        )

        fun login(sessionId: UUID, tokens: MobileTokens) = LoginByPhoneVerifyResponse(
            sessionId = sessionId,
            tokens = tokens.toServiceResponse()
        )
    }
}

data class JwtMobileTokens(
    val accessToken: String,
    val accessExpiration: Instant = Instant.now(),
    val refreshToken: String,
    val refreshExpiration: Instant = Instant.now(),
)
