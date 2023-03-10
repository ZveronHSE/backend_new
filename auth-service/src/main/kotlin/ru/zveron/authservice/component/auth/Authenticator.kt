package ru.zveron.authservice.component.auth

import mu.KLogging
import org.springframework.stereotype.Component
import ru.zveron.authservice.component.auth.model.RefreshMobileSessionRequest
import ru.zveron.authservice.component.jwt.JwtManager
import ru.zveron.authservice.component.jwt.model.MobileTokens
import ru.zveron.authservice.exception.InvalidTokenException
import ru.zveron.authservice.exception.SessionExpiredException
import ru.zveron.authservice.grpc.client.ProfileServiceClient
import ru.zveron.authservice.grpc.client.model.ProfileNotFound
import ru.zveron.authservice.persistence.SessionStorage

@Component
class Authenticator(
    private val jwtManager: JwtManager,
    private val sessionStorage: SessionStorage,
    private val profileServiceClient: ProfileServiceClient,
) {

    companion object : KLogging()

    suspend fun loginUser(fingerprint: String, profileId: Long): MobileTokens {
        val session = sessionStorage.createSession(fingerprint = fingerprint, profileId = profileId)

        return jwtManager.issueMobileTokens(
            profileId = profileId,
            session = session,
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
            sessionStorage.updateSession(decodedToken.sessionId, request.fingerprint, decodedToken.tokenIdentifier)
                ?: throw InvalidTokenException("No session wss bound to token")

        return jwtManager.issueMobileTokens(decodedToken.profileId, sessionEntity)
    }

    /**
     * throws [InvalidTokenException]
     * */
    suspend fun validateAccessToken(token: String?): Long {
        if (token == null) {
            throw InvalidTokenException("Access token is null")
        }

        val decodedToken = jwtManager.decodeAccessToken(token)
        if (decodedToken.isExpired()){
            throw InvalidTokenException()
        }

        return decodedToken.profileId
    }
}
