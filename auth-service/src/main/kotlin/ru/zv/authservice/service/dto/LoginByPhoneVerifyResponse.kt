package ru.zv.authservice.service.dto

import ru.zv.authservice.persistence.model.MOBILE_PHONE_LOGIN_ALIAS
import java.util.UUID

data class LoginByPhoneVerifyResponse(
    val sessionId: UUID,
    val flowType: String = MOBILE_PHONE_LOGIN_ALIAS,
    val tokens: JwtMobileTokens,
) {
    companion object {
        fun register(sessionId: UUID) = LoginByPhoneVerifyResponse(
            sessionId = sessionId,
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

data class JwtMobileTokens(
    val accessToken: String,
    val refreshToken: String,
)
