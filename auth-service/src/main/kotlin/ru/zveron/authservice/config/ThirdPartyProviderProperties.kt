package ru.zveron.authservice.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(value = "third-party.providers")
class ThirdPartyProviderProperties(
    val gmail: Gmail,
    val mailru: Mailru,
) {
    data class Gmail(
        val host: String,
    )

    data class Mailru(
        val host: String,
    )
}
