package ru.zveron.authservice.service.model

data class LoginByPasswordRequest(
    val loginPhone: PhoneNumber,
    val password: ByteArray,
    val fingerprint: String,
)
