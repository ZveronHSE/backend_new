package ru.zveron.authservice.service.model

import java.util.UUID

data class LoginByPhoneVerifyResponse(
    val sessionId: UUID? = null,
    val tokens: JwtMobileTokens? = null,
) {
    companion object {
        fun registration(sessionId: UUID) = LoginByPhoneVerifyResponse(
            sessionId = sessionId,
        )

        fun login(accessToken: String, refreshToken: String) = LoginByPhoneVerifyResponse(
            tokens = JwtMobileTokens(
                accessToken = accessToken,
                refreshToken = refreshToken,
            )
        )
    }
}
