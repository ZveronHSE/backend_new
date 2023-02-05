package ru.zveron.authservice.service

import io.grpc.Status
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.zveron.authservice.grpc.client.ProfileServiceClient
import ru.zveron.authservice.grpc.client.model.ProfileFound
import ru.zveron.authservice.grpc.client.model.ProfileNotFound
import ru.zveron.authservice.persistence.FlowStateStorage
import ru.zveron.authservice.persistence.model.MobilePhoneLoginStateContext
import ru.zveron.authservice.service.ServiceMapper.toProfileClientRequest
import ru.zveron.authservice.util.randomCode
import ru.zveron.authservice.util.randomDeviceFp
import ru.zveron.authservice.util.randomId
import ru.zveron.authservice.util.randomLoginFlowContext
import ru.zveron.authservice.util.randomLoginInitRequest
import ru.zveron.authservice.util.randomLoginVerifyRequest
import ru.zveron.authservice.util.randomName
import ru.zveron.authservice.util.randomPhoneNumber
import ru.zveron.authservice.util.randomSurname
import ru.zveron.authservice.util.randomTokens
import ru.zveron.authservice.webclient.NotifierClient
import ru.zveron.authservice.webclient.NotifierFailure
import ru.zveron.authservice.webclient.NotifierSuccess
import ru.zveron.authservice.webclient.model.GetVerificationCodeRequest
import java.util.UUID

class LoginByPhoneFlowServiceTest {

    private val flowStateStorage = mockk<FlowStateStorage>()

    private val notifierClient = mockk<NotifierClient>()

    private val profileClient = mockk<ProfileServiceClient>()

    private val authenticator = mockk<ru.zveron.authservice.component.auth.Authenticator>()

    private val service = LoginByPhoneFlowService(
        notifierClient = notifierClient,
        flowStateStorage = flowStateStorage,
        profileClient = profileClient,
        authenticator = authenticator,
    )

    @Test
    fun `when login by phone init is a success, then returns session id`(): Unit = runBlocking {
        val request = randomLoginInitRequest()
        val clientRequest = GetVerificationCodeRequest(request.phoneNumber.toClientPhone())
        val uuid = UUID.randomUUID()
        val code = randomCode()

        val ctxSlot = slot<MobilePhoneLoginStateContext>()

        coEvery { flowStateStorage.createContext(capture(ctxSlot)) } returns uuid
        coEvery { notifierClient.initializeVerification(clientRequest) } returns NotifierSuccess(code)

        val initResponse = service.init(request)
        initResponse.shouldNotBeNull()

        coEvery { flowStateStorage.updateContext(uuid, capture(ctxSlot)) }
        val captured = ctxSlot.captured
        captured.shouldNotBeNull()
        captured.code shouldBe code
    }

    @Test
    fun `when login by phone init and client response error, then throws NotifierClientException`(): Unit =
        runBlocking {
            val phoneNumber = randomPhoneNumber()
            val request = randomLoginInitRequest().copy(
                phoneNumber = phoneNumber
            )
            val uuid = UUID.randomUUID()

            coEvery { flowStateStorage.createContext(any()) } returns uuid
            coEvery { notifierClient.initializeVerification(any()) } returns NotifierFailure(
                code = Status.Code.UNAVAILABLE,
                message = "Client failure"
            )

            assertThrows<ru.zveron.authservice.exception.NotifierClientException> {
                service.init(request)
            }
        }

    @Test
    fun `when login by phone verify is a success, and account found, then returns tokens`(): Unit = runBlocking {
        val uuid = UUID.randomUUID()
        val initialCtx = randomLoginFlowContext().copy(
            code = randomCode(),
            fingerprint = randomDeviceFp(),
        )
        val request = randomLoginVerifyRequest().copy(
            sessionId = uuid,
            code = initialCtx.code!!,
            fingerprint = initialCtx.fingerprint,
        )
        val updatedCtx = initialCtx.copy(
            isVerified = true,
            codeAttempts = 1,
        )

        val ctxSlot = slot<MobilePhoneLoginStateContext>()

        coEvery { flowStateStorage.getContext<MobilePhoneLoginStateContext>(eq(uuid)) } returns initialCtx
        coEvery { flowStateStorage.updateContext(eq(uuid), capture(ctxSlot)) } returns updatedCtx
        coEvery { profileClient.getAccountByPhone(phoneNumber = initialCtx.phoneNumber.toProfileClientRequest()) } returns ProfileFound(
            randomId(),
            randomName(),
            randomSurname()
        )
        coEvery { authenticator.loginUser(eq(initialCtx.fingerprint), any()) } returns randomTokens()

        val verifyResponse = service.verify(request)
        verifyResponse.shouldNotBeNull()

        assertSoftly {
            verifyResponse.tokens.shouldNotBeNull()
        }

        coVerify { flowStateStorage.updateContext(eq(uuid), capture(ctxSlot)) }

        val captured = ctxSlot.captured
        captured.codeAttempts shouldBe 1
        captured.isVerified shouldBe true
    }

    @Test
    fun `when login by phone verify is a success, and account not found, then returns changes flow type`(): Unit =
        runBlocking {
            val initialCtx = randomLoginFlowContext().copy(
                code = randomCode(),
                fingerprint = randomDeviceFp(),
            )
            val uuid = UUID.randomUUID()
            val request = randomLoginVerifyRequest().copy(
                sessionId = uuid,
                code = initialCtx.code!!,
                fingerprint = initialCtx.fingerprint,
            )
            val updatedCtx = initialCtx.copy(
                isVerified = true,
                codeAttempts = 1,
            )

            val ctxSlot = slot<MobilePhoneLoginStateContext>()

            coEvery { flowStateStorage.getContext<MobilePhoneLoginStateContext>(eq(uuid)) } returns initialCtx
            coEvery { flowStateStorage.updateContext(eq(uuid), capture(ctxSlot)) } returns updatedCtx
            coEvery { profileClient.getAccountByPhone(phoneNumber = initialCtx.phoneNumber.toProfileClientRequest()) } returns ProfileNotFound
            coEvery { flowStateStorage.createContext(any()) } returns UUID.randomUUID()

            val verifyResponse = service.verify(request)
            verifyResponse.shouldNotBeNull()

            assertSoftly {
                verifyResponse.sessionId shouldNotBe request.sessionId
            }

            coVerify { flowStateStorage.updateContext(eq(uuid), capture(ctxSlot)) }

            val captured = ctxSlot.captured
            captured.isVerified shouldBe true
        }

    @Test
    fun `when login by phone verify, and code differs, then throws WrongCodeException`(): Unit = runBlocking {
        val differentCode = randomCode()
        val initialCtx = randomLoginFlowContext().copy(
            code = randomCode(),
            fingerprint = randomDeviceFp(),
        )
        val uuid = UUID.randomUUID()

        coEvery { flowStateStorage.getContext<MobilePhoneLoginStateContext>(eq(uuid)) } returns initialCtx
        val request = randomLoginVerifyRequest().copy(
            sessionId = uuid,
            code = differentCode,
            fingerprint = initialCtx.fingerprint
        )

        assertThrows<ru.zveron.authservice.exception.WrongCodeException> {
            service.verify(request)
        }
    }

    @Test
    fun `when login by phone verify, and device fp differs, then throws WrongCodeException`(): Unit = runBlocking {
        val differentFp = randomDeviceFp()
        val initialCtx = randomLoginFlowContext().copy(
            code = randomCode(),
            fingerprint = randomDeviceFp(),
        )
        val uuid = UUID.randomUUID()

        coEvery { flowStateStorage.getContext<MobilePhoneLoginStateContext>(eq(uuid)) } returns initialCtx
        val request = randomLoginVerifyRequest().copy(
            sessionId = uuid,
            code = initialCtx.code!!,
            fingerprint = differentFp,
        )

        assertThrows<ru.zveron.authservice.exception.FingerprintException> {
            service.verify(request)
        }
    }
}
