package ru.zveron.authservice.grpc.client

data class RegisterProfileByPhone(
    val name: String,
    val phone: PhoneNumber,
    val hash: String,
)
