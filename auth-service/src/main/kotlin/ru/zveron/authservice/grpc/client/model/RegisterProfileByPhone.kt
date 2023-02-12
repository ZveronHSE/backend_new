package ru.zveron.authservice.grpc.client.model

data class RegisterProfileByPhone(
    val name: String,
    val phone: PhoneNumber,
    val hash: String,
)
