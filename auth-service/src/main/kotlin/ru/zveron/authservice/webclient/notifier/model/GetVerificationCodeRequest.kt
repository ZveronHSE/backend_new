package ru.zveron.authservice.webclient.notifier.model

data class GetVerificationCodeRequest(
    val phoneNumber: String,
)
