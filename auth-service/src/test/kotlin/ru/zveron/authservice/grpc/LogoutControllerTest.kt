package ru.zveron.authservice.grpc

import com.google.protobuf.Empty
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.authservice.component.jwt.JwtManager
import ru.zveron.authservice.config.BaseAuthTest
import ru.zveron.authservice.exception.InvalidTokenException
import ru.zveron.authservice.grpc.context.AccessTokenElement
import ru.zveron.authservice.persistence.repository.SessionRepository
import ru.zveron.authservice.util.randomId
import ru.zveron.authservice.util.randomSessionEntity
import java.time.Instant

internal class LogoutControllerTest : BaseAuthTest() {

    @Autowired
    lateinit var authExternalController: AuthExternalController

    @Autowired
    lateinit var sessionRepository: SessionRepository

    @Autowired
    lateinit var jwtManager: JwtManager

    @Test
    fun `when given a logout request with valid access token, then delete the session and dont throw exception`() {
        val session = randomSessionEntity().copy(
            //advance time to avoid cron deletion
            expiresAt = Instant.now().plusSeconds(10)
        )
        val mobileTokens = jwtManager.issueMobileTokens(randomId(), session)
        val accessToken = mobileTokens.accessToken.token

        runBlocking(AccessTokenElement(accessToken)) {
            val sessionId = sessionRepository.save(session).id

            shouldNotThrowAny {
                authExternalController.performLogout(Empty.getDefaultInstance())
            }

            val expectedSession = sessionRepository.findById(sessionId!!)
            expectedSession shouldBe null
        }
    }

    @Test
    fun `when given a logout request with null access token, then throw exception`() {
        runBlocking(AccessTokenElement(null)) {

            shouldThrow<InvalidTokenException> {
                authExternalController.performLogout(Empty.getDefaultInstance())
            }
        }
    }
}
