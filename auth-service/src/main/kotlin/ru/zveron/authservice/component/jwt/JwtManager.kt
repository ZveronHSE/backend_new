package ru.zveron.authservice.component.jwt

import com.nimbusds.jwt.JWTClaimsSet
import org.springframework.stereotype.Component
import ru.zveron.authservice.component.jwt.contant.TokenConstants.SESSION_ID
import ru.zveron.authservice.component.jwt.model.AccessToken
import ru.zveron.authservice.component.jwt.model.DecodedToken
import ru.zveron.authservice.component.jwt.model.MobileTokens
import ru.zveron.authservice.component.jwt.model.RefreshToken
import ru.zveron.authservice.exception.InvalidTokenException
import ru.zveron.authservice.persistence.entity.SessionEntity

@Component
class JwtManager(
    private val jwtEncoder: JwtEncoder,
    private val jwtDecoder: JwtDecoder,
) {

    fun issueMobileTokens(
        profileId: Long,
        session: SessionEntity
    ): MobileTokens {
        val accessToken = issueAccessToken(profileId, session)
        val refreshToken = issueRefreshToken(profileId, session)
        return MobileTokens(refreshToken, accessToken)
    }

    private fun issueAccessToken(
        profileId: Long,
        session: SessionEntity
    ): AccessToken {
        val claimsBuilder = JWTClaimsSet.Builder()
            .claim(SESSION_ID, session.id)
            .subject(profileId.toString())

        return jwtEncoder.signAccess(claimsBuilder)
    }

    private fun issueRefreshToken(
        profileId: Long,
        session: SessionEntity
    ): RefreshToken {
        val claimsBuilder = JWTClaimsSet.Builder()
            .jwtID(session.tokenIdentifier.toString())
            .claim(SESSION_ID, session.id)
            .subject(profileId.toString())

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
