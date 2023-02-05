package ru.zveron.authservice.component.auth.model

data class RefreshMobileSessionRequest(
    val token: String,
    val fingerprint: String,
)
