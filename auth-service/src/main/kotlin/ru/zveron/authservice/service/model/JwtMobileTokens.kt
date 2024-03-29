package ru.zveron.authservice.service.model

import java.time.Instant

data class JwtMobileTokens(
    val accessToken: String,
    val accessExpiration: Instant,
    val refreshToken: String,
    val refreshExpiration: Instant,
)