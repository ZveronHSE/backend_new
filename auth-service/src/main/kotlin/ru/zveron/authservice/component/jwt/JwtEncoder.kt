package ru.zveron.authservice.component.jwt

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import ru.zveron.authservice.component.jwt.Constants.TOKEN_TYPE
import ru.zveron.authservice.component.jwt.model.AccessToken
import ru.zveron.authservice.component.jwt.model.RefreshToken
import ru.zveron.authservice.component.jwt.model.TokenType
import java.time.Instant
import java.util.Date

class JwtEncoder(
    private val signer: JWSSigner,
    private val algorithm: JWSAlgorithm,
    private val refreshDurationMs: Long,
    private val accessDurationMs: Long,
) {

    fun signRefresh(claimSetBuilder: JWTClaimsSet.Builder): RefreshToken {
        val expirationInstant = Instant.now().plusMillis(refreshDurationMs)
        val claimSet = claimSetBuilder
            .expirationTime(Date.from(expirationInstant))
            .claim(TOKEN_TYPE, TokenType.REFRESH)
            .build()

        val header = JWSHeader.Builder(algorithm).type(JOSEObjectType.JWT).build()
        val signedJwt = SignedJWT(header, claimSet)

        signedJwt.sign(signer)
        return RefreshToken(
            token = signedJwt.serialize(),
            expiresAt = expirationInstant,
        )
    }

    fun signAccess(claimSetBuilder: JWTClaimsSet.Builder): AccessToken {
        val expirationInstant = Instant.now().plusMillis(accessDurationMs)
        val claimSet = claimSetBuilder
            .expirationTime(Date.from(expirationInstant))
            .claim(TOKEN_TYPE, TokenType.ACCESS)
            .build()

        val header = JWSHeader.Builder(algorithm).type(JOSEObjectType.JWT).build()
        val signedJwt = SignedJWT(header, claimSet)

        signedJwt.sign(signer)
        return AccessToken(
            token = signedJwt.serialize(),
            expiresAt = expirationInstant,
        )
    }
}
