package ru.zv.authservice.component.jwt

import com.nimbusds.jwt.JWTClaimNames.EXPIRATION_TIME
import com.nimbusds.jwt.JWTClaimNames.ISSUER
import com.nimbusds.jwt.JWTClaimNames.JWT_ID
import com.nimbusds.jwt.JWTClaimNames.SUBJECT
import ru.zv.authservice.component.jwt.Constants.SESSION_ID
import ru.zv.authservice.component.jwt.Constants.TOKEN_TYPE
import ru.zv.authservice.component.jwt.model.DecodedToken
import ru.zv.authservice.component.jwt.model.TokenType
import ru.zv.authservice.exceptions.InvalidTokenException
import ru.zv.authservice.service.dto.JwtMobileTokens
import java.time.Instant
import java.util.UUID

object JwtMapper {
    fun MobileTokens.toServiceResponse() = JwtMobileTokens(
        accessToken = this.accessToken.token,
        accessExpiration = this.accessToken.expiresAt,
        refreshToken = this.refreshToken.token,
        refreshExpiration = this.refreshToken.expiresAt,
    )

    fun Map<String, Any>.toDecodedToken() = DecodedToken(
        profileId = this[SUBJECT]?.let { (it as String).toLongOrNull() } ?: throw InvalidTokenException(),
        tokenType = this[TOKEN_TYPE]?.let { TokenType.fromName(it as String) } ?: throw InvalidTokenException(),
        issuer = this[ISSUER]?.let { it as String } ?: throw InvalidTokenException(),
        expiresAt = this[EXPIRATION_TIME]?.let { Instant.ofEpochSecond((it as Long)) } ?: throw InvalidTokenException(),
        sessionId = this[SESSION_ID]?.let { UUID.fromString(it as String) } ?: throw InvalidTokenException(),
        tokenIdentifier = this[JWT_ID]?.let { UUID.fromString(it as String) } ?: throw InvalidTokenException(),
    )
}
