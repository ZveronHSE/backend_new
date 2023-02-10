package ru.zveron.authservice.component.jwt.model

import java.time.Instant

data class RefreshToken(
    val token: String,
    val expiresAt: Instant,
)