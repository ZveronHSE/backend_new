package ru.zveron.authservice.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder

@Configuration
@EnableConfigurationProperties(ArgonConfigurationProperties::class)
class PasswordEncoderConfig(
    private val properties: ArgonConfigurationProperties,
) {

    @Bean
    fun argon2PasswordEncoder(): Argon2PasswordEncoder {
        return Argon2PasswordEncoder(
            properties.saltLength,
            properties.hashLength,
            properties.parallelism,
            properties.memorySizeKiB,
            properties.iterations,
        )
    }
}

@ConfigurationProperties("zveron.argon2i")
data class ArgonConfigurationProperties(
    val iterations: Int = 5,
    val memorySizeKiB: Int = 7168,
    val parallelism: Int = 1,
    val saltLength: Int = 16,
    val hashLength: Int = 32,
)
