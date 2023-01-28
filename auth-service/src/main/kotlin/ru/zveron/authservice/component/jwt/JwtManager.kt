package ru.zveron.authservice.component.jwt

import com.nimbusds.jwt.JWTClaimsSet
import org.springframework.stereotype.Component
import ru.zveron.authservice.component.jwt.Constants.SESSION_ID
import ru.zveron.authservice.component.jwt.Constants.ZV_ISSUER
import ru.zveron.authservice.component.jwt.model.DecodedToken
import ru.zveron.authservice.exception.InvalidTokenException
import ru.zveron.authservice.persistence.entity.SessionEntity

@Component
class JwtManager(
    private val jwtEncoder: JwtEncoder,
    private val jwtDecoder: JwtDecoder,
) {

    fun issueMobileTokens(request: IssueMobileTokensRequest): MobileTokens {
        val accessToken = issueMobileAccessToken(request)
        val refreshToken = issueRefreshToken(request)
        return MobileTokens(refreshToken, accessToken)
    }

    private fun issueMobileAccessToken(request: IssueMobileTokensRequest): AccessToken {
        val claimsBuilder = JWTClaimsSet.Builder()
            .jwtID(request.session.tokenIdentifier.toString())
            .issuer(ZV_ISSUER)
            .claim(SESSION_ID, request.session.id)
            .subject(request.profileId.toString())

        return jwtEncoder.signAccess(claimsBuilder)
    }

    private fun issueRefreshToken(request: IssueMobileTokensRequest): RefreshToken {
        val claimsBuilder = JWTClaimsSet.Builder()
            .jwtID(request.session.tokenIdentifier.toString())
            .issuer(ZV_ISSUER)
            .claim(SESSION_ID, request.session.id)
            .subject(request.profileId.toString())

        return jwtEncoder.signRefresh(claimsBuilder)
    }

    /**
     * throws [InvalidTokenException]
     * */
    fun decodeAccessToken(token: String): DecodedToken {
        return jwtDecoder.decodeAccessToken(token)
    }

    /**
     * throws [InvalidTokenException]
     * */
    fun decodeRefreshToken(token: String): DecodedToken {
        return jwtDecoder.decodeRefreshToken(token)
    }
}

data class IssueMobileTokensRequest(
    val profileId: Long,
    val session: SessionEntity,
)

data class MobileTokens(
    val refreshToken: RefreshToken,
    val accessToken: AccessToken,
)