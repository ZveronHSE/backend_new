package ru.zv.authservice.component.auth

import mu.KLogging
import org.springframework.stereotype.Component
import ru.zv.authservice.component.jwt.IssueMobileTokensRequest
import ru.zv.authservice.component.jwt.JwtManager
import ru.zv.authservice.component.jwt.MobileTokens
import ru.zv.authservice.exceptions.InvalidTokenException
import ru.zv.authservice.exceptions.SessionExpiredException
import ru.zv.authservice.grpc.client.ProfileServiceClient
import ru.zv.authservice.grpc.client.dto.ProfileNotFound
import ru.zv.authservice.persistence.SessionStorage

@Component
class Authenticator(
    private val jwtManager: JwtManager,
    private val sessionStorage: SessionStorage,
    private val profileServiceClient: ProfileServiceClient,
) {

    companion object : KLogging()

    suspend fun loginUser(fp: String, profileId: Long): MobileTokens {
        val session = sessionStorage.createSession(
            fp = fp,
            profileId = profileId,
        )

        return jwtManager.issueMobileTokens(
            IssueMobileTokensRequest(
                profileId = profileId,
                session = session,
            )
        )
    }

    /**
     * throws [InvalidTokenException]
     * throws [SessionExpiredException]
     * */
    suspend fun refreshMobileSession(request: RefreshMobileSessionRequest): MobileTokens {
        val decodedToken = jwtManager.decodeRefreshToken(token = request.token)

        val profileResponse = profileServiceClient.getProfileById(decodedToken.profileId)

        if (profileResponse is ProfileNotFound) {
            throw InvalidTokenException("Profile not found")
        }

        val sessionEntity =
            sessionStorage.updateSession(decodedToken.sessionId, request.fp, decodedToken.tokenIdentifier)
                ?: throw InvalidTokenException("No session wss bound to token")

        return jwtManager.issueMobileTokens(IssueMobileTokensRequest(decodedToken.profileId, sessionEntity))
    }

    /**
     * throws [InvalidTokenException]
     * */
    suspend fun validateAccessToken(token: String) {
        jwtManager.decodeAccessToken(token)
    }
}

data class RefreshMobileSessionRequest(
    val token: String,
    val fp: String,
)
