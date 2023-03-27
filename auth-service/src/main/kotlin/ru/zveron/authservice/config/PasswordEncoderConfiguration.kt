package ru.zveron.authservice.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder

@Configuration
@EnableConfigurationProperties(ArgonConfigurationProperties::class)
class PasswordEncoderConfiguration(
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

