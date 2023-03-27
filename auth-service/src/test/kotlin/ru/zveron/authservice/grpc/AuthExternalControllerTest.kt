package ru.zveron.authservice.grpc

import io.grpc.Status
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.authservice.config.BaseAuthTest
import ru.zveron.authservice.grpc.client.model.ProfileFound
import ru.zveron.authservice.grpc.client.model.ProfileNotFound
import ru.zveron.authservice.persistence.FlowStateStorage
import ru.zveron.authservice.persistence.entity.StateContextEntity
import ru.zveron.authservice.persistence.model.MobilePhoneLoginStateContext
import ru.zveron.authservice.persistence.model.MobilePhoneRegisterStateContext
import ru.zveron.authservice.service.mapper.ServiceMapper.toProfileClientRequest
import ru.zveron.authservice.util.randomCode
import ru.zveron.authservice.util.randomDeviceFp
import ru.zveron.authservice.util.randomId
import ru.zveron.authservice.util.randomLoginFlowContext
import ru.zveron.authservice.util.randomLoginInitApigRequest
import ru.zveron.authservice.util.randomLoginVerifyApigRequest
import ru.zveron.authservice.util.randomName
import ru.zveron.authservice.util.randomPhoneNumber
import ru.zveron.authservice.util.randomSurname
import ru.zveron.authservice.webclient.notifier.NotifierFailure
import ru.zveron.authservice.webclient.notifier.NotifierSuccess
import ru.zveron.authservice.webclient.notifier.model.GetVerificationCodeRequest
import ru.zveron.contract.auth.external.copy
import ru.zveron.contract.auth.external.mobileTokenOrNull
import java.util.UUID

internal class AuthExternalControllerTest : BaseAuthTest() {

    @Autowired
    lateinit var authExternalController: AuthExternalController

    @Autowired
    lateinit var flowStateStorage: FlowStateStorage

    @Test
    fun `when login by phone init is a success, then returns session id`() = runBlocking {
        val phoneNumber = randomPhoneNumber()
        val request = randomLoginInitApigRequest().copy {
            this.phoneNumber = phoneNumber.toClientPhone()
        }
        val clientRequest = GetVerificationCodeRequest(phoneNumber.toClientPhone())
        val code = randomCode()

        coEvery { notifierClient.initializeVerification(clientRequest) } returns NotifierSuccess(code)

        val initResponse = authExternalController.phoneLoginInit(request)
        initResponse.shouldNotBeNull()

        val stateContextEntity = template.select(StateContextEntity::class.java).all().awaitSingle()
        stateContextEntity.shouldNotBeNull()
        stateContextEntity.sessionId shouldBe UUID.fromString(initResponse.sessionId)
    }

    @Test
    fun `when login by phone init and client response error, then throws NotifierClientException`(): Unit =
        runBlocking {
            val phoneNumber = randomPhoneNumber()
            val request = randomLoginInitApigRequest().copy {
                this.phoneNumber = phoneNumber.toClientPhone()
            }

            coEvery { notifierClient.initializeVerification(any()) } returns NotifierFailure(
                code = Status.Code.UNAVAILABLE,
                message = "Client failure"
            )

            assertThrows<ru.zveron.authservice.exception.NotifierClientException> {
                authExternalController.phoneLoginInit(request)
            }
        }

    @Test
    fun `when login by phone verify is a success, and account found, then returns tokens`(): Unit = runBlocking {
        val initialCtx = randomLoginFlowContext().copy(
            code = randomCode(),
            fingerprint = randomDeviceFp(),
        )
        val uuid = flowStateStorage.createContext(initialCtx)
        val request = randomLoginVerifyApigRequest().copy {
            this.sessionId = uuid.toString()
            this.code = initialCtx.code!!
            this.deviceFp = initialCtx.fingerprint
        }

        coEvery { profileClient.getProfileByPhone(phoneNumber = initialCtx.phoneNumber.toProfileClientRequest()) } returns ProfileFound(
            randomId(),
            randomName(),
            randomSurname()
        )

        val verifyResponse = authExternalController.phoneLoginVerify(request)
        verifyResponse.shouldNotBeNull()

        assertSoftly {
            verifyResponse.mobileTokenOrNull.shouldNotBeNull()
        }

        val ctxEntity = template.select(StateContextEntity::class.java).all().awaitSingle()
        val updatedCtx =
            ctxEntity.data.asString().let { objectMapper.readValue(it, MobilePhoneLoginStateContext::class.java) }
        assertSoftly {
            updatedCtx.isVerified shouldBe true
            updatedCtx.codeAttempts shouldBe 1
        }
    }

    @Test
    fun `when login by phone verify is a success, and account not found, then returns changes flow type`() =
        runBlocking {
            val initialCtx = randomLoginFlowContext().copy(
                code = randomCode(),
                fingerprint = randomDeviceFp(),
            )
            val uuid = flowStateStorage.createContext(initialCtx)
            val request = randomLoginVerifyApigRequest().copy {
                this.sessionId = uuid.toString()
                this.code = initialCtx.code!!
                this.deviceFp = initialCtx.fingerprint
            }

            coEvery { profileClient.getProfileByPhone(phoneNumber = initialCtx.phoneNumber.toProfileClientRequest()) } returns ProfileNotFound

            val verifyResponse = authExternalController.phoneLoginVerify(request)
            verifyResponse.shouldNotBeNull()
            assertSoftly {
                verifyResponse.sessionId shouldNotBe request.sessionId
            }

            val ctxEntity = template.select(StateContextEntity::class.java).all().awaitLast()
            val registerFlowContext =
                ctxEntity.data.asString()
                    .let { objectMapper.readValue(it, MobilePhoneRegisterStateContext::class.java) }
            registerFlowContext.shouldNotBeNull()
            assertSoftly {
                registerFlowContext.isChannelVerified shouldBe true
                registerFlowContext.phoneNumber shouldBe initialCtx.phoneNumber
                registerFlowContext.fingerprint shouldBe initialCtx.fingerprint
            }
        }

    @Test
    fun `when login by phone verify, and code differs, then throws WrongCodeException`(): Unit = runBlocking {
        val differentCode = randomCode()
        val initialCtx = randomLoginFlowContext().copy(
            code = randomCode(),
            fingerprint = randomDeviceFp(),
        )
        val uuid = flowStateStorage.createContext(initialCtx)
        val request = randomLoginVerifyApigRequest().copy {
            this.sessionId = uuid.toString()
            this.code = differentCode
            this.deviceFp = initialCtx.fingerprint
        }

        assertThrows<ru.zveron.authservice.exception.WrongCodeException> {
            authExternalController.phoneLoginVerify(request)
        }
    }

    @Test
    fun `when login by phone verify, and code channel already verified, then throws CodeValidatedException`(): Unit =
        runBlocking {
            val initialCtx = randomLoginFlowContext().copy(
                code = randomCode(),
                fingerprint = randomDeviceFp(),
                isVerified = true
            )
            val uuid = flowStateStorage.createContext(initialCtx)
            val request = randomLoginVerifyApigRequest().copy {
                this.sessionId = uuid.toString()
                this.code = initialCtx.code!!
                this.deviceFp = initialCtx.fingerprint
            }

            assertThrows<ru.zveron.authservice.exception.CodeValidatedException> {
                authExternalController.phoneLoginVerify(request)
            }
        }

    @Test
    fun `when login by phone verify, and device fp differs, then throws ex`(): Unit = runBlocking {
        val differentFp = randomDeviceFp()
        val initialCtx = randomLoginFlowContext().copy(
            code = randomCode(),
            fingerprint = randomDeviceFp(),
        )
        val uuid = flowStateStorage.createContext(initialCtx)
        val request = randomLoginVerifyApigRequest().copy {
            this.sessionId = uuid.toString()
            this.code = initialCtx.code!!
            this.deviceFp = differentFp
        }
        assertThrows<ru.zveron.authservice.exception.FingerprintException> {
            authExternalController.phoneLoginVerify(request)
        }
    }
}
