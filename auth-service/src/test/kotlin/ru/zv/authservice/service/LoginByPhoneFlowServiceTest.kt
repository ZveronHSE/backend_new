package ru.zv.authservice.service

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
import ru.zv.authservice.exceptions.FingerprintException
import ru.zv.authservice.exceptions.NotifierClientException
import ru.zv.authservice.exceptions.WrongCodeException
import ru.zv.authservice.grpc.client.ProfileServiceClient
import ru.zv.authservice.grpc.client.dto.ProfileFound
import ru.zv.authservice.grpc.client.dto.ProfileNotFound
import ru.zv.authservice.persistence.FlowStateStorage
import ru.zv.authservice.persistence.model.MobilePhoneLoginStateContext
import ru.zv.authservice.service.dto.toContext
import ru.zv.authservice.util.randomCode
import ru.zv.authservice.util.randomDeviceFp
import ru.zv.authservice.util.randomId
import ru.zv.authservice.util.randomLoginFlowContext
import ru.zv.authservice.util.randomLoginInitRequest
import ru.zv.authservice.util.randomLoginVerifyRequest
import ru.zv.authservice.util.randomName
import ru.zv.authservice.util.randomPhoneNumber
import ru.zv.authservice.util.randomSurname
import ru.zv.authservice.webclient.NotifierClient
import ru.zv.authservice.webclient.NotifierFailure
import ru.zv.authservice.webclient.NotifierSuccess
import ru.zv.authservice.webclient.dto.GetVerificationCodeRequest
import java.util.UUID

class LoginByPhoneFlowServiceTest {

    private val flowStateStorage = mockk<FlowStateStorage>()

    private val notifierClient = mockk<NotifierClient>()

    private val profileClient = mockk<ProfileServiceClient>()

    private val service = LoginByPhoneFlowService(
        notifierClient = notifierClient,
        flowStateStorage = flowStateStorage,
        profileClient = profileClient,
    )

    @Test
    fun `when login by phone init is a success, then returns session id`(): Unit = runBlocking {
        val request = randomLoginInitRequest()
        val clientRequest = GetVerificationCodeRequest(request.phoneNumber.toClientPhone())
        val ctx = randomLoginFlowContext().copy(
            phoneNumber = request.phoneNumber.toContext(),
        )
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

            assertThrows<NotifierClientException> {
                service.init(request)
            }
        }


    @Test
    fun `when login by phone verify is a success, and account found, then returns tokens`(): Unit = runBlocking {
        val uuid = UUID.randomUUID()
        val initialCtx = randomLoginFlowContext().copy(
            code = randomCode(),
            deviceFp = randomDeviceFp(),
        )
        val request = randomLoginVerifyRequest().copy(
            sessionId = uuid,
            code = initialCtx.code!!,
            deviceFp = initialCtx.deviceFp,
        )
        val updatedCtx = initialCtx.copy(
            isVerified = true,
            codeAttempts = 1,
        )

        val ctxSlot = slot<MobilePhoneLoginStateContext>()

        coEvery { flowStateStorage.getContext<MobilePhoneLoginStateContext>(eq(uuid)) } returns initialCtx
        coEvery { flowStateStorage.updateContext(eq(uuid), capture(ctxSlot)) } returns updatedCtx
        coEvery { profileClient.getAccountByPhone(phoneNumber = initialCtx.phoneNumber) } returns ProfileFound(
            randomId(),
            randomName(),
            randomSurname()
        )

        val verifyResponse = service.verify(request)
        verifyResponse.shouldNotBeNull()

        assertSoftly {
            verifyResponse.isNewUser shouldBe false
            verifyResponse.sessionId shouldBe request.sessionId
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
                deviceFp = randomDeviceFp(),
            )
            val uuid = UUID.randomUUID()
            val request = randomLoginVerifyRequest().copy(
                sessionId = uuid,
                code = initialCtx.code!!,
                deviceFp = initialCtx.deviceFp,
            )
            val updatedCtx = initialCtx.copy(
                isVerified = true,
                codeAttempts = 1,
            )

            val ctxSlot = slot<MobilePhoneLoginStateContext>()

            coEvery { flowStateStorage.getContext<MobilePhoneLoginStateContext>(eq(uuid)) } returns initialCtx
            coEvery { flowStateStorage.updateContext(eq(uuid), capture(ctxSlot)) } returns updatedCtx
            coEvery { profileClient.getAccountByPhone(phoneNumber = initialCtx.phoneNumber) } returns ProfileNotFound
            coEvery { flowStateStorage.createContext(any()) } returns UUID.randomUUID()

            val verifyResponse = service.verify(request)
            verifyResponse.shouldNotBeNull()

            assertSoftly {
                verifyResponse.isNewUser shouldBe true
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
            deviceFp = randomDeviceFp(),
        )
        val uuid = UUID.randomUUID()

        coEvery { flowStateStorage.getContext<MobilePhoneLoginStateContext>(eq(uuid)) } returns initialCtx
        val request = randomLoginVerifyRequest().copy(
            sessionId = uuid,
            code = differentCode,
            deviceFp = initialCtx.deviceFp
        )

        assertThrows<WrongCodeException> {
            service.verify(request)
        }
    }

    @Test
    fun `when login by phone verify, and device fp differs, then throws WrongCodeException`(): Unit = runBlocking {
        val differentFp = randomDeviceFp()
        val initialCtx = randomLoginFlowContext().copy(
            code = randomCode(),
            deviceFp = randomDeviceFp(),
        )
        val uuid = UUID.randomUUID()

        coEvery { flowStateStorage.getContext<MobilePhoneLoginStateContext>(eq(uuid)) } returns initialCtx
        val request = randomLoginVerifyRequest().copy(
            sessionId = uuid,
            code = initialCtx.code!!,
            deviceFp = differentFp,
        )

        assertThrows<FingerprintException> {
            service.verify(request)
        }
    }

}
