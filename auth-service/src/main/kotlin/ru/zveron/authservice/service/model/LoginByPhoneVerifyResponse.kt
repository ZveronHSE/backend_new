package ru.zveron.authservice.service.model

import ru.zveron.authservice.component.jwt.model.MobileTokens
import java.util.UUID

data class LoginByPhoneVerifyResponse(
    val sessionId: UUID? = null,
    val tokens: JwtMobileTokens? = null,
) {
    companion object {
        fun registration(sessionId: UUID) = LoginByPhoneVerifyResponse(
            sessionId = sessionId,
        )

        fun login(tokens: MobileTokens) = LoginByPhoneVerifyResponse(
            tokens = JwtMobileTokens(
                accessToken = tokens.accessToken.token,
                accessExpiration = tokens.accessToken.expiresAt,
                refreshToken = tokens.refreshToken.token,
                refreshExpiration = tokens.refreshToken.expiresAt,
            )
        )
    }
}
