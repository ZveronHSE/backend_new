package ru.zv.authservice.config

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.MACVerifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated
import ru.zv.authservice.component.jwt.JwtDecoder
import ru.zv.authservice.component.jwt.JwtEncoder

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class JwtConfiguration(
    private val properties: JwtProperties,
) {

    @Bean
    fun signer() = JwtEncoder(
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

@Validated
@ConstructorBinding
@ConfigurationProperties("zv.jwt")
data class JwtProperties(

    val secret: String,

    val accessDurationMs: Long,

    val refreshDurationMs: Long,
)