package ru.zveron.authservice.grpc.client.model

data class ValidatePasswordRequest(
    val phoneNumber: String,
    val passwordHash: String,
)