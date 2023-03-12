package ru.zveron.authservice.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated

@Validated
@ConstructorBinding
@ConfigurationProperties("zveron.jwt")
data class JwtProperties(

    val secret: String,

    val accessDurationMs: Long,

    val refreshDurationMs: Long,
)