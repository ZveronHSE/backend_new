package ru.zveron.authservice.webclient.dto

data class GetVerificationCodeRequest(
    val phoneNumber: String,
)
