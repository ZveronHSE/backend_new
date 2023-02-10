package ru.zveron.authservice.component.jwt.model

data class MobileTokens(
    val refreshToken: RefreshToken,
    val accessToken: AccessToken,
)