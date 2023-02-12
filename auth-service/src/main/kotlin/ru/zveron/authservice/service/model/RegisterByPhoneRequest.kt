package ru.zveron.authservice.service.model

import java.util.UUID

data class RegisterByPhoneRequest(
    val fingerprint: String,
    val sessionId: UUID,
    val password: ByteArray,
    val name: String,
    val surname: String,
)
