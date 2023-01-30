package ru.zveron.authservice.component.auth

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import ru.zveron.authservice.component.jwt.JwtManager
import ru.zveron.authservice.component.jwt.model.IssueMobileTokensRequest
import ru.zveron.authservice.exception.InvalidTokenException
import ru.zveron.authservice.exception.SessionExpiredException
import ru.zveron.authservice.grpc.client.ProfileServiceClient
import ru.zveron.authservice.grpc.client.model.ProfileFound
import ru.zveron.authservice.grpc.client.model.ProfileNotFound
import ru.zveron.authservice.persistence.SessionStorage
import ru.zveron.authservice.util.randomDecodedToken
import ru.zveron.authservice.util.randomDeviceFp
import ru.zveron.authservice.util.randomName
import ru.zveron.authservice.util.randomSessionEntity
import ru.zveron.authservice.util.randomSurname
import ru.zveron.authservice.util.randomTokens
import java.util.UUID

class AuthenticatorTest {

    private val jwtManager = mockk<JwtManager>()
    private val profileServiceClient = mockk<ProfileServiceClient>()
    private val sessionStorage = mockk<SessionStorage>()

    private val service = ru.zveron.authservice.component.auth.Authenticator(
        jwtManager = jwtManager,
        sessionStorage = sessionStorage,
        profileServiceClient = profileServiceClient,
    )

    @Test
    fun `refresh mobile session smoke test`(): Unit = runBlocking {

        val token = UUID.randomUUID().toString()
        val fp = randomDeviceFp()
        val decodedToken = randomDecodedToken()
        val profile = ProfileFound(decodedToken.profileId, randomName(), randomSurname())
        val session = randomSessionEntity()
        val issueTokensRequest = IssueMobileTokensRequest(profile.id, session)
        val refreshMobileSessionReq = RefreshMobileSessionRequest(token, fp)

        coEvery { jwtManager.decodeRefreshToken(eq(token)) } returns decodedToken
        coEvery { profileServiceClient.getProfileById(eq(decodedToken.profileId)) } returns profile
        coEvery {
            sessionStorage.updateSession(
                eq(decodedToken.sessionId),
                eq(fp),
                decodedToken.tokenIdentifier
            )
        } returns session
        coEvery { jwtManager.issueMobileTokens(eq(issueTokensRequest)) } returns randomTokens()

        assertDoesNotThrow {
            service.refreshMobileSession(refreshMobileSessionReq)
        }
    }

    @Test
    fun `when refresh mobile session and refresh token decoding throws InvalidTokenException, then throws exception`(): Unit =
        runBlocking {
            val token = UUID.randomUUID().toString()
            val fp = randomDeviceFp()
            val refreshMobileSessionReq = RefreshMobileSessionRequest(token, fp)

            coEvery { jwtManager.decodeRefreshToken(eq(token)) } throws InvalidTokenException()

            assertThrows<InvalidTokenException> {
                service.refreshMobileSession(refreshMobileSessionReq)
            }
        }

    @Test
    fun `when refresh mobile session and profile not found, then throws InvalidTokenException`(): Unit =
        runBlocking {
            val token = UUID.randomUUID().toString()
            val fp = randomDeviceFp()
            val decodedToken = randomDecodedToken()
            val refreshMobileSessionReq = RefreshMobileSessionRequest(token, fp)

            coEvery { jwtManager.decodeRefreshToken(eq(token)) } returns decodedToken
            coEvery { profileServiceClient.getProfileById(eq(decodedToken.profileId)) } returns ProfileNotFound


            assertThrows<InvalidTokenException> {
                service.refreshMobileSession(refreshMobileSessionReq)
            }
        }

    @Test
    fun `when refresh mobile session and session storage throws exception, then throw exception`(): Unit = runBlocking {
        val token = UUID.randomUUID().toString()
        val fp = randomDeviceFp()
        val decodedToken = randomDecodedToken()
        val profile = ProfileFound(decodedToken.profileId, randomName(), randomSurname())
        val refreshMobileSessionReq = RefreshMobileSessionRequest(token, fp)

        coEvery { jwtManager.decodeRefreshToken(eq(token)) } returns decodedToken
        coEvery { profileServiceClient.getProfileById(eq(decodedToken.profileId)) } returns profile
        coEvery {
            sessionStorage.updateSession(
                eq(decodedToken.sessionId),
                eq(fp),
                decodedToken.tokenIdentifier
            )
        } throws SessionExpiredException()

        assertThrows<SessionExpiredException> {
            service.refreshMobileSession(refreshMobileSessionReq)
        }
    }
}
