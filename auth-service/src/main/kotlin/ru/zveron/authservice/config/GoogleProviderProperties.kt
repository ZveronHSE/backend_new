package ru.zveron.authservice.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(value = "third-party.providers.google")
class GoogleProviderProperties(
    val host: String,
)
