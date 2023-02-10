package ru.zveron.authservice.service.model

data class LoginByPhoneInitRequest(
    val phoneNumber: PhoneNumber,
    val fingerprint: String,
)
