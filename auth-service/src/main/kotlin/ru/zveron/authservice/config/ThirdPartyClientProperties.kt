package ru.zveron.authservice.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(value = "clients.third-party")
data class ThirdPartyClientProperties(
    val maxConnections: Int = 10,
    val connectionTimeoutMs: Int = 5000,
    val readTimeoutMs: Long = 5000L,
)