package ru.zveron.authservice.component.jwt

import com.nimbusds.jwt.JWTClaimsSet
import org.springframework.stereotype.Component
import ru.zveron.authservice.component.jwt.Constants.SESSION_ID
import ru.zveron.authservice.component.jwt.Constants.ZV_ISSUER
import ru.zveron.authservice.component.jwt.model.AccessToken
import ru.zveron.authservice.component.jwt.model.DecodedToken
import ru.zveron.authservice.component.jwt.model.IssueMobileTokensRequest
import ru.zveron.authservice.component.jwt.model.MobileTokens
import ru.zveron.authservice.component.jwt.model.RefreshToken
import ru.zveron.authservice.exception.InvalidTokenException

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
