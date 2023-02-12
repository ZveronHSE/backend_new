package ru.zveron.authservice.grpc

import com.google.protobuf.kotlin.toByteStringUtf8
import io.grpc.Status
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.InternalPlatformDsl.toStr
import io.mockk.coEvery
import io.r2dbc.postgresql.codec.Json
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.authservice.component.jwt.JwtDecoder
import ru.zveron.authservice.config.BaseAuthTest
import ru.zveron.authservice.exception.ContextExpiredException
import ru.zveron.authservice.exception.FingerprintException
import ru.zveron.authservice.exception.RegistrationException
import ru.zveron.authservice.grpc.client.model.RegisterProfileAlreadyExists
import ru.zveron.authservice.grpc.client.model.RegisterProfileFailure
import ru.zveron.authservice.grpc.client.model.RegisterProfileSuccess
import ru.zveron.authservice.persistence.entity.StateContextEntity
import ru.zveron.authservice.persistence.model.MobilePhoneRegisterStateContext
import ru.zveron.authservice.util.randomDeviceFp
import ru.zveron.authservice.util.randomId
import ru.zveron.authservice.util.randomName
import ru.zveron.authservice.util.randomPassword
import ru.zveron.authservice.util.randomPersistencePhone
import ru.zveron.authservice.util.randomSurname
import ru.zveron.contract.auth.phoneRegisterRequest
import java.util.UUID

class AuthRegistrationFlowTest : BaseAuthTest() {

    @Autowired
    lateinit var controller: AuthLoginController

    @Autowired
    lateinit var jwtDecoder: JwtDecoder

    @Test
    fun `happy pass registration`() = runBlocking {
        //given
        val sessionId = UUID.randomUUID()
        val phoneNumber = randomPersistencePhone()
        val fingerprint = randomDeviceFp()

        //login flow finished and we now have a registration flow context saved
        val registrationStateContextEntity = MobilePhoneRegisterStateContext(
            phoneNumber = phoneNumber,
            fingerprint = fingerprint,
            isChannelVerified = true,
        )
        val stateContextEntity = StateContextEntity(
            sessionId = sessionId,
            data = Json.of(objectMapper.writeValueAsBytes(registrationStateContextEntity)),
        )

        template.insert(stateContextEntity).awaitSingle()

        //when we register new account, we respond success and a random id
        val newProfileId = randomId()
        coEvery { profileClient.registerProfileByPhone(any()) } returns RegisterProfileSuccess(newProfileId)

        //we have name, surname, password and registration sessionId of a person to register
        val surname = randomSurname()
        val name = randomName()
        val password = randomPassword()

        val registerRequest = phoneRegisterRequest {
            this.deviceFp = fingerprint
            this.sessionId = sessionId.toStr()
            this.name = name
            this.surname = surname
            this.password = password.toByteStringUtf8()
        }

        //when
        val response = controller.registerByPhone(registerRequest)

        //then

        //should not throw exceptions and response not null
        response.shouldNotBeNull()

        //make sure new access token is bound to account
        val decodedAccessToken = jwtDecoder.decodeAccessToken(response.accessToken.token)
        decodedAccessToken.profileId shouldBe newProfileId

        val decodedRefreshToken = jwtDecoder.decodeRefreshToken(response.refreshToken.token)
        decodedRefreshToken.profileId shouldBe newProfileId
    }

    @Test
    fun `when context exists with another sessionId and try to register, then throw exception`(): Unit = runBlocking {
        //given
        val sessionId = UUID.randomUUID()
        val phoneNumber = randomPersistencePhone()
        val fingerprint = randomDeviceFp()

        //login flow finished and we now have a registration flow context saved
        val registrationStateContextEntity = MobilePhoneRegisterStateContext(
            phoneNumber = phoneNumber,
            fingerprint = fingerprint,
            isChannelVerified = true,
        )
        val stateContextEntity = StateContextEntity(
            //another sessionId
            sessionId = UUID.randomUUID(),
            data = Json.of(objectMapper.writeValueAsBytes(registrationStateContextEntity)),
        )

        template.insert(stateContextEntity).awaitSingle()

        //we have name, surname, password and registration sessionId of a person to register
        val surname = randomSurname()
        val name = randomName()
        val password = randomPassword()

        val registerRequest = phoneRegisterRequest {
            this.deviceFp = fingerprint
            this.sessionId = sessionId.toStr()
            this.name = name
            this.surname = surname
            this.password = password.toByteStringUtf8()
        }

        //when
        assertThrows<ContextExpiredException> {
            controller.registerByPhone(registerRequest)
        }
    }

    @Test
    fun `when context found, but profile client exists, then throw exception`(): Unit = runBlocking {
        //given
        val sessionId = UUID.randomUUID()
        val phoneNumber = randomPersistencePhone()
        val fingerprint = randomDeviceFp()

        //login flow finished and we now have a registration flow context saved
        val registrationStateContextEntity = MobilePhoneRegisterStateContext(
            phoneNumber = phoneNumber,
            fingerprint = fingerprint,
            isChannelVerified = true,
        )
        val stateContextEntity = StateContextEntity(
            sessionId = sessionId,
            data = Json.of(objectMapper.writeValueAsBytes(registrationStateContextEntity)),
        )

        template.insert(stateContextEntity).awaitSingle()

        //when we register new account, returns already exists
        coEvery { profileClient.registerProfileByPhone(any()) } returns RegisterProfileAlreadyExists

        //we have name, surname, password and registration sessionId of a person to register
        val surname = randomSurname()
        val name = randomName()
        val password = randomPassword()

        val registerRequest = phoneRegisterRequest {
            this.deviceFp = fingerprint
            this.sessionId = sessionId.toStr()
            this.name = name
            this.surname = surname
            this.password = password.toByteStringUtf8()
        }

        //when
        assertThrows<RegistrationException> {
            controller.registerByPhone(registerRequest)
        }
    }

    @Test
    fun `when fingerprint doesnt match, then throw exception`(): Unit = runBlocking {
        //given
        val sessionId = UUID.randomUUID()
        val phoneNumber = randomPersistencePhone()
        val fingerprint = randomDeviceFp()

        //login flow finished and we now have a registration flow context saved
        val registrationStateContextEntity = MobilePhoneRegisterStateContext(
            phoneNumber = phoneNumber,
            fingerprint = fingerprint,
            isChannelVerified = true,
        )
        val stateContextEntity = StateContextEntity(
            sessionId = sessionId,
            data = Json.of(objectMapper.writeValueAsBytes(registrationStateContextEntity)),
        )

        template.insert(stateContextEntity).awaitSingle()

        //we have name, surname, password and registration sessionId of a person to register
        val surname = randomSurname()
        val name = randomName()
        val password = randomPassword()

        val registerRequest = phoneRegisterRequest {
            //different fingerprint
            this.deviceFp = randomDeviceFp()
            this.sessionId = sessionId.toStr()
            this.name = name
            this.surname = surname
            this.password = password.toByteStringUtf8()
        }

        //when
        assertThrows<FingerprintException> {
            controller.registerByPhone(registerRequest)
        }
    }

    @Test
    fun `when context is present, but profile client responds unknown error, then throw exception`(): Unit =
        runBlocking {
            //given
            val sessionId = UUID.randomUUID()
            val phoneNumber = randomPersistencePhone()
            val fingerprint = randomDeviceFp()

            //login flow finished and we now have a registration flow context saved
            val registrationStateContextEntity = MobilePhoneRegisterStateContext(
                phoneNumber = phoneNumber,
                fingerprint = fingerprint,
                isChannelVerified = true,
            )
            val stateContextEntity = StateContextEntity(
                sessionId = sessionId,
                data = Json.of(objectMapper.writeValueAsBytes(registrationStateContextEntity)),
            )

            template.insert(stateContextEntity).awaitSingle()

            //when we register new account, we respond unknown failure
            coEvery { profileClient.registerProfileByPhone(any()) } returns RegisterProfileFailure(code = Status.INTERNAL)

            //we have name, surname, password and registration sessionId of a person to register
            val surname = randomSurname()
            val name = randomName()
            val password = randomPassword()

            val registerRequest = phoneRegisterRequest {
                this.deviceFp = fingerprint
                this.sessionId = sessionId.toStr()
                this.name = name
                this.surname = surname
                this.password = password.toByteStringUtf8()
            }

            //when
            assertThrows<RegistrationException> {
                controller.registerByPhone(registerRequest)
            }
        }
}
