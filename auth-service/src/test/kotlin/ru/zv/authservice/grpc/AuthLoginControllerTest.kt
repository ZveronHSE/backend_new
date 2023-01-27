package ru.zv.authservice.grpc

import io.grpc.Status
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import ru.zv.authservice.config.BaseAuthTest
import ru.zv.authservice.exceptions.CodeValidatedException
import ru.zv.authservice.exceptions.FingerprintException
import ru.zv.authservice.exceptions.NotifierClientException
import ru.zv.authservice.exceptions.WrongCodeException
import ru.zv.authservice.grpc.client.dto.ProfileFound
import ru.zv.authservice.grpc.client.dto.ProfileNotFound
import ru.zv.authservice.persistence.FlowStateStorage
import ru.zv.authservice.persistence.entity.StateContextEntity
import ru.zv.authservice.persistence.model.MOBILE_PHONE_LOGIN_ALIAS
import ru.zv.authservice.persistence.model.MobilePhoneLoginStateContext
import ru.zv.authservice.util.randomCode
import ru.zv.authservice.util.randomDeviceFp
import ru.zv.authservice.util.randomId
import ru.zv.authservice.util.randomLoginFlowContext
import ru.zv.authservice.util.randomLoginInitApigRequest
import ru.zv.authservice.util.randomLoginVerifyApigRequest
import ru.zv.authservice.util.randomName
import ru.zv.authservice.util.randomPhoneNumber
import ru.zv.authservice.util.randomSurname
import ru.zv.authservice.webclient.NotifierFailure
import ru.zv.authservice.webclient.NotifierSuccess
import ru.zv.authservice.webclient.dto.GetVerificationCodeRequest
import ru.zveron.contract.auth.copy
import java.util.UUID

internal class AuthLoginControllerTest : BaseAuthTest() {

    @Autowired
    lateinit var authLoginController: AuthLoginController

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

        val initResponse = authLoginController.phoneLoginInit(request)
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

            assertThrows<NotifierClientException> {
                authLoginController.phoneLoginInit(request)
            }
        }

    @Test
    fun `when login by phone verify is a success, and account found, then returns tokens`(): Unit = runBlocking {
        val initialCtx = randomLoginFlowContext().copy(
            code = randomCode(),
            deviceFp = randomDeviceFp(),
        )
        val uuid = flowStateStorage.createContext(initialCtx)
        val request = randomLoginVerifyApigRequest().copy {
            this.sessionId = uuid.toString()
            this.code = initialCtx.code!!
            this.deviceFp = initialCtx.deviceFp
        }

        coEvery { profileClient.getAccountByPhone(phoneNumber = initialCtx.phoneNumber) } returns ProfileFound(
            randomId(),
            randomName(),
            randomSurname()
        )

        val verifyResponse = authLoginController.phoneLoginVerify(request)
        verifyResponse.shouldNotBeNull()

        assertSoftly {
            verifyResponse.isNewUser shouldBe false
            verifyResponse.sessionId shouldBe request.sessionId
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
                deviceFp = randomDeviceFp(),
            )
            val uuid = flowStateStorage.createContext(initialCtx)
            val request = randomLoginVerifyApigRequest().copy {
                this.sessionId = uuid.toString()
                this.code = initialCtx.code!!
                this.deviceFp = initialCtx.deviceFp
            }

            coEvery { profileClient.getAccountByPhone(phoneNumber = initialCtx.phoneNumber) } returns ProfileNotFound

            val verifyResponse = authLoginController.phoneLoginVerify(request)
            verifyResponse.shouldNotBeNull()
            assertSoftly {
                verifyResponse.isNewUser shouldBe true
                verifyResponse.sessionId shouldBe request.sessionId
                //todo tokens gen
            }

            val ctxEntity = template.select(StateContextEntity::class.java).all().awaitSingle()
            val registerFlowContext =
                ctxEntity.data.asString()
                    .let { objectMapper.readValue(it, MobilePhoneLoginStateContext::class.java) }
            registerFlowContext.shouldNotBeNull()
            assertSoftly {
                registerFlowContext.isVerified shouldBe true
                registerFlowContext.phoneNumber shouldBe initialCtx.phoneNumber
                registerFlowContext.deviceFp shouldBe initialCtx.deviceFp
            }
        }

    @Test
    fun `when login by phone verify, and code differs, then throws WrongCodeException`(): Unit = runBlocking {
        val differentCode = randomCode()
        val initialCtx = randomLoginFlowContext().copy(
            code = randomCode(),
            deviceFp = randomDeviceFp(),
        )
        val uuid = flowStateStorage.createContext(initialCtx)
        val request = randomLoginVerifyApigRequest().copy {
            this.sessionId = uuid.toString()
            this.code = differentCode
            this.deviceFp = initialCtx.deviceFp
        }

        assertThrows<WrongCodeException> {
            authLoginController.phoneLoginVerify(request)
        }
    }

    @Test
    fun `when login by phone verify, and code channel already verified, then throws CodeValidatedException`(): Unit =
        runBlocking {
            val initialCtx = randomLoginFlowContext().copy(
                code = randomCode(),
                deviceFp = randomDeviceFp(),
                isVerified = true
            )
            val uuid = flowStateStorage.createContext(initialCtx)
            val request = randomLoginVerifyApigRequest().copy {
                this.sessionId = uuid.toString()
                this.code = initialCtx.code!!
                this.deviceFp = initialCtx.deviceFp
            }

            assertThrows<CodeValidatedException> {
                authLoginController.phoneLoginVerify(request)
            }
        }

    @Test
    fun `when login by phone verify, and device fp differs, then throws ex`(): Unit = runBlocking {
        val differentFp = randomDeviceFp()
        val initialCtx = randomLoginFlowContext().copy(
            code = randomCode(),
            deviceFp = randomDeviceFp(),
        )
        val uuid = flowStateStorage.createContext(initialCtx)
        val request = randomLoginVerifyApigRequest().copy {
            this.sessionId = uuid.toString()
            this.code = initialCtx.code!!
            this.deviceFp = differentFp
        }
        assertThrows<FingerprintException> {
            authLoginController.phoneLoginVerify(request)
        }
    }
}
