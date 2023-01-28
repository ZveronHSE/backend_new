package ru.zv.authservice.component

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.MACVerifier
import com.nimbusds.jwt.JWTClaimsSet
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import org.apache.commons.lang3.RandomUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.zv.authservice.component.jwt.Constants.SESSION_ID
import ru.zv.authservice.component.jwt.Constants.ZV_ISSUER
import ru.zv.authservice.component.jwt.JwtDecoder
import ru.zv.authservice.component.jwt.JwtEncoder
import ru.zv.authservice.component.jwt.model.TokenType
import ru.zv.authservice.exceptions.InvalidTokenException
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

    private val jwtDecoder = JwtDecoder(
        verifier = MACVerifier(secret)
    )

    @Test
    fun `when access token encoded with signer, decoder can verify and provide correct payload`() {
        val id = RandomUtils.nextLong()
        val identifier = UUID.randomUUID()
        val sessionId = UUID.randomUUID()
        val claimBuilder = JWTClaimsSet.Builder().issuer(ZV_ISSUER)
            .jwtID(identifier.toString())
            .subject(id.toString())
            .claim(SESSION_ID, sessionId)

        val encodedToken = jwtEncoder.signAccess(claimBuilder)

        val decodedToken = jwtDecoder.decodeAccessToken(encodedToken.token)

        assertSoftly {
            decodedToken.tokenType shouldBe TokenType.ACCESS
            decodedToken.issuer shouldBe ZV_ISSUER
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
        val claimBuilder = JWTClaimsSet.Builder().issuer(ZV_ISSUER)
            .jwtID(identifier.toString())
            .subject(id.toString())
            .claim(SESSION_ID, sessionId)

        val encodedToken = jwtEncoder.signRefresh(claimBuilder)

        val decodedToken = jwtDecoder.decodeRefreshToken(encodedToken.token)

        assertSoftly {
            decodedToken.tokenType shouldBe TokenType.REFRESH
            decodedToken.issuer shouldBe ZV_ISSUER
            decodedToken.expiresAt.epochSecond shouldBe encodedToken.expiresAt.epochSecond
            decodedToken.profileId shouldBe id
            decodedToken.sessionId shouldBe sessionId
        }
    }

    @Test
    fun `when access decoded with a different token type, then throw InvalidTokenException`() {
        val id = RandomUtils.nextLong()
        val sessionId = UUID.randomUUID()
        val claimBuilder = JWTClaimsSet.Builder().issuer(ZV_ISSUER)
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
        val claimBuilder = JWTClaimsSet.Builder().issuer(ZV_ISSUER)
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
        val differentDecoder = JwtDecoder(MACVerifier(differentSecret))

        val id = RandomUtils.nextLong()
        val sessionId = UUID.randomUUID()
        val claimBuilder = JWTClaimsSet.Builder().issuer(ZV_ISSUER)
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
        val differentDecoder = JwtDecoder(MACVerifier(differentSecret))

        val id = RandomUtils.nextLong()
        val sessionId = UUID.randomUUID()
        val claimBuilder = JWTClaimsSet.Builder().issuer("Zveron")
            .subject(id.toString())
            .claim(SESSION_ID, sessionId)

        val encodedToken = jwtEncoder.signRefresh(claimBuilder)

        assertThrows<InvalidTokenException> {
            differentDecoder.decodeRefreshToken(encodedToken.token)
        }
    }
}