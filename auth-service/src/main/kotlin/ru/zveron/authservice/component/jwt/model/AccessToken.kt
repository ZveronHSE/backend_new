package ru.zveron.authservice.component.jwt.model

import java.time.Instant

data class AccessToken(
    val token: String,
    val expiresAt: Instant,
)