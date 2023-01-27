package ru.zv.authservice.grpc

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
import ru.zv.authservice.config.BaseAuthTest
import ru.zv.authservice.exceptions.CodeValidatedException
import ru.zv.authservice.grpc.client.dto.ProfileFound
import ru.zv.authservice.grpc.client.dto.ProfileNotFound
import ru.zv.authservice.persistence.entity.StateContextEntity
import ru.zv.authservice.persistence.model.MobilePhoneLoginStateContext
import ru.zv.authservice.persistence.model.MobilePhoneRegisterStateContext
import ru.zv.authservice.service.dto.toContext
import ru.zv.authservice.util.randomCode
import ru.zv.authservice.util.randomDeviceFp
import ru.zv.authservice.util.randomId
import ru.zv.authservice.util.randomLoginInitApigRequest
import ru.zv.authservice.util.randomName
import ru.zv.authservice.util.randomPhoneNumber
import ru.zv.authservice.util.randomSurname
import ru.zv.authservice.webclient.NotifierSuccess
import ru.zv.authservice.webclient.dto.GetVerificationCodeRequest
import ru.zveron.contract.auth.copy
import ru.zveron.contract.auth.phoneLoginVerifyRequest

class AuthLoginFullFlowTest : BaseAuthTest() {

    @Autowired
    lateinit var authLoginController: AuthLoginController

    @Test
    fun `when login by phone init is a success and verify is success and finds account, then returns response with tokens`() =
        runBlocking {
            //given when init
            val deviceFp = randomDeviceFp()
            val phoneNumber = randomPhoneNumber()
            val request = randomLoginInitApigRequest().copy {
                this.phoneNumber = phoneNumber.toClientPhone()
                this.deviceFp = deviceFp
            }
            val clientRequest = GetVerificationCodeRequest(phoneNumber.toClientPhone())
            val code = randomCode()

            coEvery { notifierClient.initializeVerification(clientRequest) } returns NotifierSuccess(code)

            //when
            val initResponse = authLoginController.phoneLoginInit(request)

            val verifyRequest = phoneLoginVerifyRequest {
                this.deviceFp = deviceFp
                this.code = code
                this.sessionId = initResponse.sessionId
            }

            coEvery { profileClient.getAccountByPhone(any()) } returns ProfileFound(
                randomId(),
                randomName(),
                randomSurname()
            )

            val verifyResponse = authLoginController.phoneLoginVerify(verifyRequest)
            verifyResponse.shouldNotBeNull()

            assertSoftly {
                verifyResponse.isNewUser shouldBe false
                verifyResponse.sessionId shouldBe initResponse.sessionId
            }

            val ctxEntity = template.select(StateContextEntity::class.java).all().awaitSingle()
            val updatedCtx =
                ctxEntity.data.asString().let { objectMapper.readValue(it, MobilePhoneLoginStateContext::class.java) }

            assertSoftly {
                updatedCtx.isVerified shouldBe true
                updatedCtx.codeAttempts shouldBe 1
                updatedCtx.deviceFp shouldBe deviceFp
                updatedCtx.phoneNumber shouldBe phoneNumber.toContext()
            }
        }

    @Test
    fun `when login by phone init is a success and verify is success and account not found, then returns response with register ctx`() =
        runBlocking {
            //given when init
            val deviceFp = randomDeviceFp()
            val phoneNumber = randomPhoneNumber()
            val request = randomLoginInitApigRequest().copy {
                this.phoneNumber = phoneNumber.toClientPhone()
                this.deviceFp = deviceFp
            }
            val clientRequest = GetVerificationCodeRequest(phoneNumber.toClientPhone())
            val code = randomCode()

            coEvery { notifierClient.initializeVerification(clientRequest) } returns NotifierSuccess(code)

            //when
            val initResponse = authLoginController.phoneLoginInit(request)

            val verifyRequest = phoneLoginVerifyRequest {
                this.deviceFp = deviceFp
                this.code = code
                this.sessionId = initResponse.sessionId
            }

            coEvery { profileClient.getAccountByPhone(any()) } returns ProfileNotFound

            val verifyResponse = authLoginController.phoneLoginVerify(verifyRequest)
            verifyResponse.shouldNotBeNull()

            assertSoftly {
                verifyResponse.isNewUser shouldBe true
                verifyResponse.sessionId shouldNotBe initResponse.sessionId
            }

            val ctxEntity = template.select(StateContextEntity::class.java).all().awaitLast()
            val updatedCtx =
                ctxEntity.data.asString()
                    .let { objectMapper.readValue(it, MobilePhoneRegisterStateContext::class.java) }

            assertSoftly {
                updatedCtx.isChannelVerified shouldBe true
                updatedCtx.deviceFp shouldBe deviceFp
                updatedCtx.phoneNumber shouldBe phoneNumber.toContext()
            }
        }

    @Test
    fun `when login by phone init is a success and verify is success and new verify request, then returns throws CodeVerifiedException`(): Unit =
        runBlocking {
            //given when init
            val deviceFp = randomDeviceFp()
            val phoneNumber = randomPhoneNumber()
            val request = randomLoginInitApigRequest().copy {
                this.phoneNumber = phoneNumber.toClientPhone()
                this.deviceFp = deviceFp
            }
            val clientRequest = GetVerificationCodeRequest(phoneNumber.toClientPhone())
            val code = randomCode()

            coEvery { notifierClient.initializeVerification(clientRequest) } returns NotifierSuccess(code)

            //when
            val initResponse = authLoginController.phoneLoginInit(request)

            val verifyRequest = phoneLoginVerifyRequest {
                this.deviceFp = deviceFp
                this.code = code
                this.sessionId = initResponse.sessionId
            }

            coEvery { profileClient.getAccountByPhone(any()) } returns ProfileNotFound

            val verifyResponse = authLoginController.phoneLoginVerify(verifyRequest)
            verifyResponse.shouldNotBeNull()

            assertSoftly {
                verifyResponse.isNewUser shouldBe true
                verifyResponse.sessionId shouldNotBe initResponse.sessionId
            }

            val ctxEntity = template.select(StateContextEntity::class.java).all().awaitLast()
            val updatedCtx =
                ctxEntity.data.asString()
                    .let { objectMapper.readValue(it, MobilePhoneRegisterStateContext::class.java) }

            assertSoftly {
                updatedCtx.isChannelVerified shouldBe true
                updatedCtx.deviceFp shouldBe deviceFp
                updatedCtx.phoneNumber shouldBe phoneNumber.toContext()
            }

            //here throws exception
            assertThrows<CodeValidatedException> {
                authLoginController.phoneLoginVerify(verifyRequest)
            }
        }
}
