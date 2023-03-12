package ru.zveron.authservice.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("zveron.argon2i")
data class ArgonConfigurationProperties(
    val iterations: Int = 5,
    val memorySizeKiB: Int = 7168,
    val parallelism: Int = 1,
    val saltLength: Int = 16,
    val hashLength: Int = 32,
)