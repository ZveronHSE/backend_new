package ru.zveron.authservice.service

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import ru.zveron.authservice.component.jwt.JwtManager
import ru.zveron.authservice.exception.InvalidTokenException
import ru.zveron.authservice.persistence.repository.SessionRepository
import ru.zveron.authservice.util.randomAccessToken
import ru.zveron.authservice.util.randomDecodedToken
import java.time.Instant

class LogoutServiceTest {

    private val sessionRepository = mockk<SessionRepository>()

    private val jwtManager = mockk<JwtManager>()

    private val logoutService = LogoutService(
        sessionRepository = sessionRepository,
        jwtManager = jwtManager,
    )

    @Test
    fun `when given a valid token, then run flow successfully`() {
        val accessToken = randomAccessToken()
        val decodedToken = randomDecodedToken()

        coEvery { jwtManager.decodeAccessToken(accessToken.token) } returns decodedToken
        coEvery { sessionRepository.deleteById(any()) } just Runs

        runBlocking {
            shouldNotThrowAny {
                logoutService.logout(accessToken = accessToken.token)
            }
        }
    }

    @Test
    fun `when given an invalid token, then throw token exception`() {
        val accessToken = randomAccessToken()

        coEvery { jwtManager.decodeAccessToken(accessToken.token) } throws InvalidTokenException()

        runBlocking {
            shouldThrow<InvalidTokenException> {
                logoutService.logout(accessToken = accessToken.token)
            }
        }
    }

    @Test
    fun `when given an expired token, then run flow successfully`() {
        val accessToken = randomAccessToken()
        val decodedToken = randomDecodedToken().copy(expiresAt = Instant.now().minusSeconds(10))

        coEvery { jwtManager.decodeAccessToken(accessToken.token) } returns decodedToken
        coEvery { sessionRepository.deleteById(any()) } just Runs

        runBlocking {
            shouldNotThrowAny {
                logoutService.logout(accessToken = accessToken.token)
            }
        }
    }
}
