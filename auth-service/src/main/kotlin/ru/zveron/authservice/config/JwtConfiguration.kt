package ru.zveron.authservice.config

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.MACVerifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.zveron.authservice.component.jwt.JwtDecoder
import ru.zveron.authservice.component.jwt.JwtEncoder

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class JwtConfiguration(
    private val properties: JwtProperties,
) {

    @Bean
    fun encoder() = JwtEncoder(
        signer = MACSigner(properties.secret),
        algorithm = JWSAlgorithm.HS256,
        accessDurationMs = properties.accessDurationMs,
        refreshDurationMs = properties.refreshDurationMs,
    )

    @Bean
    fun decoder() = JwtDecoder(
        verifier = MACVerifier(properties.secret)
    )
}
