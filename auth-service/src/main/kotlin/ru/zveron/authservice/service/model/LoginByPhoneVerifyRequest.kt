package ru.zveron.authservice.service.model

import java.util.UUID

data class LoginByPhoneVerifyRequest(
    val code: String,
    val sessionId: UUID,
    val fingerprint: String,
)
