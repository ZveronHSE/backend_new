package ru.zveron.authservice.grpc

import com.google.protobuf.Empty
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.nulls.shouldNotBeNull
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
import ru.zveron.authservice.grpc.context.AccessTokenElement
import ru.zveron.authservice.persistence.entity.StateContextEntity
import ru.zveron.authservice.service.mapper.ServiceMapper.toContext
import ru.zveron.authservice.util.randomCode
import ru.zveron.authservice.util.randomDeviceFp
import ru.zveron.authservice.util.randomId
import ru.zveron.authservice.util.randomLoginFlowContext
import ru.zveron.authservice.util.randomName
import ru.zveron.authservice.util.randomPhoneNumber
import ru.zveron.authservice.util.randomSurname
import ru.zveron.contract.auth.external.issueNewTokensRequest
import ru.zveron.contract.auth.external.mobileTokenOrNull
import ru.zveron.contract.auth.external.phoneLoginVerifyRequest
import java.util.UUID

class AuthTokensTest : BaseAuthTest() {

    @Autowired
    lateinit var authExternalController: AuthExternalController

    @Autowired
    lateinit var authInternalController: AuthInternalController

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
                fingerprint = deviceFp,
                phoneNumber = phoneNumber.toContext(),
                code = code,
            )

            val entity = StateContextEntity(
                sessionId = sessionId,
                data = Json.of(objectMapper.writeValueAsBytes(loginStateContext)),
            )

            template.insert(entity).awaitSingle()

            coEvery { profileClient.getProfileByPhone(any()) } returns profileFoundResponse
            coEvery { profileClient.findProfileById(eq(profileId)) } returns profileFoundResponse

            val verifyRequest = phoneLoginVerifyRequest {
                this.deviceFp = deviceFp
                this.code = code
                this.sessionId = sessionId.toString()
            }

            val verifyResponse = authExternalController.phoneLoginVerify(verifyRequest)
            verifyResponse.shouldNotBeNull()

            assertSoftly {
                verifyResponse.mobileTokenOrNull.shouldNotBeNull()
            }

            val refreshToken = verifyResponse.mobileToken.refreshToken
            val accessToken = verifyResponse.mobileToken.accessToken

            //access token passes validation
            assertDoesNotThrow {
                runBlocking(AccessTokenElement(accessToken.token)) {
                    authInternalController.verifyToken(Empty.getDefaultInstance())
                }
            }

            //can get new tokens from
            val newTokens = authExternalController.issueNewTokens(request = issueNewTokensRequest {
                this.deviceFp = deviceFp
                this.refreshToken = refreshToken.token
            })

            //new access token passes validation
            assertDoesNotThrow {
                runBlocking(AccessTokenElement(newTokens.accessToken.token)) {
                    authInternalController.verifyToken(Empty.getDefaultInstance())
                }
            }

            //old refresh token fails
            assertThrows<InvalidTokenException> {
                authExternalController.issueNewTokens(request = issueNewTokensRequest {
                    this.deviceFp = deviceFp
                    this.refreshToken = refreshToken.token
                })
            }
        }
}
