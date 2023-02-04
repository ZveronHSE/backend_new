package ru.zveron.authservice.grpc

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.r2dbc.postgresql.codec.Json
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.authservice.config.BaseAuthTest
import ru.zveron.authservice.exception.InvalidTokenException
import ru.zveron.authservice.grpc.client.model.ProfileFound
import ru.zveron.authservice.persistence.entity.StateContextEntity
import ru.zveron.authservice.service.ServiceMapper.toContext
import ru.zveron.authservice.util.randomCode
import ru.zveron.authservice.util.randomDeviceFp
import ru.zveron.authservice.util.randomId
import ru.zveron.authservice.util.randomLoginFlowContext
import ru.zveron.authservice.util.randomName
import ru.zveron.authservice.util.randomPhoneNumber
import ru.zveron.authservice.util.randomSurname
import ru.zveron.contract.auth.issueNewTokensRequest
import ru.zveron.contract.auth.phoneLoginVerifyRequest
import ru.zveron.contract.auth.verifyMobileTokenRequest
import java.util.UUID

class AuthTokensTest : BaseAuthTest() {

    @Autowired
    lateinit var authLoginController: AuthLoginController

    @Test
    fun `when logged in, then return valid tokens, that pass validation and can be used to refresh session`(): Unit =
        runBlocking {
            //given when init
            val deviceFp = randomDeviceFp()
            val phoneNumber = randomPhoneNumber()
            val sessionId = UUID.randomUUID()
            val profileId = randomId()
            val profileName = randomName()
            val profileSurname = randomSurname()

            val profileFoundResponse = ProfileFound(
                profileId,
                profileName,
                profileSurname
            )

            val code = randomCode()

            val loginStateContext = randomLoginFlowContext().copy(
                deviceFp = deviceFp,
                phoneNumber = phoneNumber.toContext(),
                code = code,
            )

            val entity = StateContextEntity(
                sessionId = sessionId,
                data = Json.of(objectMapper.writeValueAsBytes(loginStateContext)),
            )

            template.insert(entity).awaitSingle()

            coEvery { profileClient.getAccountByPhone(any()) } returns profileFoundResponse
            coEvery { profileClient.getProfileById(eq(profileId)) } returns profileFoundResponse

            val verifyRequest = phoneLoginVerifyRequest {
                this.deviceFp = deviceFp
                this.code = code
                this.sessionId = sessionId.toString()
            }

            val verifyResponse = authLoginController.phoneLoginVerify(verifyRequest)
            verifyResponse.shouldNotBeNull()

            assertSoftly {
                verifyResponse.isNewUser shouldBe false
                verifyResponse.sessionId shouldBe sessionId.toString()
            }

            val refreshToken = verifyResponse.mobileToken.refreshToken
            val accessToken = verifyResponse.mobileToken.accessToken

            //access token passes validation
            assertDoesNotThrow {
                authLoginController.verifyToken(verifyMobileTokenRequest {
                    this.accessToken = accessToken.token
                })
            }

            //can get new tokens from
            val newTokens = authLoginController.issueNewTokens(request = issueNewTokensRequest {
                this.deviceFp = deviceFp
                this.refreshToken = refreshToken.token
            })

            //new access token passes validation
            assertDoesNotThrow {
                authLoginController.verifyToken(verifyMobileTokenRequest {
                    this.accessToken = newTokens.accessToken.token
                })
            }

            //old refresh token fails
            assertThrows<InvalidTokenException> {
                authLoginController.issueNewTokens(request = issueNewTokensRequest {
                    this.deviceFp = deviceFp
                    this.refreshToken = refreshToken.token
                })
            }
        }
}