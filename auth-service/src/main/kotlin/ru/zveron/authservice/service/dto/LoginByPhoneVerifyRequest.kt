package ru.zveron.authservice.service.dto

import java.util.UUID

data class LoginByPhoneVerifyRequest(
    val code: String,
    val sessionId: UUID,
    val deviceFp: String,
)
