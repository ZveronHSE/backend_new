package ru.zv.authservice.service.dto

import ru.zv.authservice.persistence.model.MOBILE_PHONE_LOGIN_ALIAS
import ru.zv.authservice.persistence.model.MOBILE_PHONE_REGISTER_ALIAS
import java.util.UUID

data class LoginByPhoneVerifyResponse(
    val sessionId: UUID,
    val flowType: String,
    val tokens: JwtMobileTokens,
) {
    companion object {
        fun register(sessionId: UUID) = LoginByPhoneVerifyResponse(
            sessionId = sessionId,
            flowType = MOBILE_PHONE_REGISTER_ALIAS,
            tokens = JwtMobileTokens(
                accessToken = "",
                refreshToken = "",
            )
        )

        fun login(sessionId: UUID, accessToken: String, refreshToken: String) = LoginByPhoneVerifyResponse(
            sessionId = sessionId,
            flowType = MOBILE_PHONE_LOGIN_ALIAS,
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