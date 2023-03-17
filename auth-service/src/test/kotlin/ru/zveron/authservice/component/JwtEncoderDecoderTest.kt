package ru.zveron.authservice.component

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.MACVerifier
import com.nimbusds.jwt.JWTClaimsSet
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import org.apache.commons.lang3.RandomUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.zveron.authservice.component.jwt.contant.TokenConstants.SESSION_ID
import ru.zveron.authservice.component.jwt.JwtEncoder
import ru.zveron.authservice.component.jwt.contant.TokenType
import ru.zveron.authservice.exception.InvalidTokenException
import java.util.UUID

class JwtEncoderDecoderTest {

    companion object {
        const val accessDurationMs = 10_000L
        const val refreshDurationMs = 100_000L
        const val secret = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    }

    private val jwtEncoder = JwtEncoder(
        signer = MACSigner(secret),
        algorithm = JWSAlgorithm.HS256,
        accessDurationMs = accessDurationMs,
        refreshDurationMs = refreshDurationMs,
    )

    private val jwtDecoder = ru.zveron.authservice.component.jwt.JwtDecoder(
        verifier = MACVerifier(secret)
    )

    @Test
    fun `when access token encoded with signer, decoder can verify and provide correct payload`() {
        val id = RandomUtils.nextLong()
        val identifier = UUID.randomUUID()
        val sessionId = UUID.randomUUID()
        val claimBuilder = JWTClaimsSet.Builder()
            .jwtID(identifier.toString())
            .subject(id.toString())
            .claim(SESSION_ID, sessionId)

        val encodedToken = jwtEncoder.signAccess(claimBuilder)

        val decodedToken = jwtDecoder.decodeAccessToken(encodedToken.token)

        assertSoftly {
            decodedToken.tokenType shouldBe TokenType.ACCESS
            decodedToken.expiresAt.epochSecond shouldBe encodedToken.expiresAt.epochSecond
            decodedToken.profileId shouldBe id
            decodedToken.sessionId shouldBe sessionId
        }
    }

    @Test
    fun `when refresh token encoded with signer, decoder can verify and provide correct payload`() {
        val id = RandomUtils.nextLong()
        val sessionId = UUID.randomUUID()
        val identifier = UUID.randomUUID()
        val claimBuilder = JWTClaimsSet.Builder()
            .jwtID(identifier.toString())
            .subject(id.toString())
            .claim(SESSION_ID, sessionId)

        val encodedToken = jwtEncoder.signRefresh(claimBuilder)

        val decodedToken = jwtDecoder.decodeRefreshToken(encodedToken.token)

        assertSoftly {
            decodedToken.tokenType shouldBe TokenType.REFRESH
            decodedToken.expiresAt.epochSecond shouldBe encodedToken.expiresAt.epochSecond
            decodedToken.profileId shouldBe id
            decodedToken.sessionId shouldBe sessionId
        }
    }

    @Test
    fun `when access decoded with a different token type, then throw InvalidTokenException`() {
        val id = RandomUtils.nextLong()
        val sessionId = UUID.randomUUID()
        val claimBuilder = JWTClaimsSet.Builder()
            .subject(id.toString())
            .claim(SESSION_ID, sessionId)

        val encodedToken = jwtEncoder.signAccess(claimBuilder)

        assertThrows<InvalidTokenException> {
            jwtDecoder.decodeRefreshToken(encodedToken.token)
        }
    }

    @Test
    fun `when refresh decoded with a different token type, then throw InvalidTokenException`() {
        val id = RandomUtils.nextLong()
        val sessionId = UUID.randomUUID()
        val claimBuilder = JWTClaimsSet.Builder()
            .subject(id.toString())
            .claim(SESSION_ID, sessionId)

        val encodedToken = jwtEncoder.signRefresh(claimBuilder)

        assertThrows<InvalidTokenException> {
            jwtDecoder.decodeAccessToken(encodedToken.token)
        }
    }

    @Test
    fun `when access decoded with a different secret decoder, then throw InvalidTokenException`() {
        val differentSecret = UUID.randomUUID().toString()
        val differentDecoder = ru.zveron.authservice.component.jwt.JwtDecoder(MACVerifier(differentSecret))

        val id = RandomUtils.nextLong()
        val sessionId = UUID.randomUUID()
        val claimBuilder = JWTClaimsSet.Builder()
            .subject(id.toString())
            .claim(SESSION_ID, sessionId)

        val encodedToken = jwtEncoder.signAccess(claimBuilder)

        assertThrows<InvalidTokenException> {
            differentDecoder.decodeAccessToken(encodedToken.token)
        }
    }

    @Test
    fun `when refresh decoded with a different secret decoder, then throw InvalidTokenException`() {
        val differentSecret = UUID.randomUUID().toString()
        val differentDecoder = ru.zveron.authservice.component.jwt.JwtDecoder(MACVerifier(differentSecret))

        val id = RandomUtils.nextLong()
        val sessionId = UUID.randomUUID()
        val claimBuilder = JWTClaimsSet.Builder()
            .subject(id.toString())
            .claim(SESSION_ID, sessionId)

        val encodedToken = jwtEncoder.signRefresh(claimBuilder)

        assertThrows<InvalidTokenException> {
            differentDecoder.decodeRefreshToken(encodedToken.token)
        }
    }
}
