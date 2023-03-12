package ru.zveron.authservice.grpc.client.model

data class RegisterByPhoneRequest(
    val name: String,
    val phone: PhoneNumber,
    val hash: String,
)
