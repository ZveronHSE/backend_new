package ru.zveron.authservice.component.jwt

import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jwt.JWTClaimNames.EXPIRATION_TIME
import ru.zveron.authservice.component.jwt.Constants.TOKEN_TYPE
import ru.zveron.authservice.component.jwt.JwtMapper.toDecodedToken
import ru.zveron.authservice.component.jwt.model.DecodedToken
import ru.zveron.authservice.component.jwt.model.TokenType
import ru.zveron.authservice.exception.InvalidTokenException
import ru.zveron.authservice.exception.TokenExpiredException
import java.time.Instant

class JwtDecoder(
    private val verifier: JWSVerifier,
) {

    /**
     * throws [InvalidTokenException]
     * */
    fun decodeAccessToken(token: String): DecodedToken {
        val jsonObj = verifyAndGetPayload(token, TokenType.ACCESS)

        return jsonObj.toDecodedToken()
    }

    /**
     * throws [InvalidTokenException]
     * */
    fun decodeRefreshToken(token: String): DecodedToken {
        val jsonObj = verifyAndGetPayload(token, TokenType.REFRESH)

        return jsonObj.toDecodedToken()
    }

    /**
     * throws [InvalidTokenException]
      */
    private fun verifyAndGetPayload(token: String, tokenType: TokenType): MutableMap<String, Any> {
        val jwsObject = JWSObject.parse(token)

        if (!jwsObject.verify(verifier)) {
            throw InvalidTokenException()
        }

        val payload = jwsObject.payload
        val jsonObject = payload.toJSONObject()

        jsonObject[TOKEN_TYPE]?.let { TokenType.fromName(it as String) }?.takeIf { it == tokenType }
            ?: throw InvalidTokenException()

        jsonObject[EXPIRATION_TIME]?.let { Instant.ofEpochSecond(it as Long) }?.takeIf { it.isAfter(Instant.now()) }
            ?: throw TokenExpiredException()

        return jsonObject
    }
}
