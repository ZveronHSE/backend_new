package ru.zveron.authservice.service.model

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

        fun login(sessionId: UUID, accessToken: String, refreshToken: String) = LoginByPhoneVerifyResponse(
            sessionId = sessionId,
            tokens = JwtMobileTokens(
                accessToken = accessToken,
                refreshToken = refreshToken,
            )
        )
    }
}
