package ru.zveron.authservice.service

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import ru.zveron.authservice.component.auth.Authenticator
import ru.zveron.authservice.exception.ChannelNotVerifiedException
import ru.zveron.authservice.exception.ContextExpiredException
import ru.zveron.authservice.exception.FingerprintException
import ru.zveron.authservice.exception.RegistrationException
import ru.zveron.authservice.grpc.client.ProfileServiceClient
import ru.zveron.authservice.grpc.client.model.PhoneNumber
import ru.zveron.authservice.grpc.client.model.RegisterProfileAlreadyExists
import ru.zveron.authservice.grpc.client.model.RegisterProfileSuccess
import ru.zveron.authservice.persistence.FlowStateStorage
import ru.zveron.authservice.persistence.model.MobilePhoneRegisterStateContext
import ru.zveron.authservice.util.randomDeviceFp
import ru.zveron.authservice.util.randomHash
import ru.zveron.authservice.util.randomId
import ru.zveron.authservice.util.randomName
import ru.zveron.authservice.util.randomPersistencePhone
import ru.zveron.authservice.util.randomSurname
import ru.zveron.authservice.util.randomTokens
import java.util.UUID

class RegistrationServiceTest {

    private val flowStorage = mockk<FlowStateStorage>()
    private val profileServiceClient = mockk<ProfileServiceClient>()
    private val authenticator = mockk<Authenticator>()
    private val argon2PasswordEncoder = mockk<Argon2PasswordEncoder>()

    private val service = RegistrationService(
        flowStateStorage = flowStorage,
        profileServiceClient = profileServiceClient,
        authenticator = authenticator,
        argon2Encoder = argon2PasswordEncoder,
    )

    @Test
    fun `when register profile request and channel is verified and client returns success and authenticator returns tokens, then return tokens`() {
        return runBlocking {
            //given

            //request data
            val sessionId = UUID.randomUUID()
            val fingerprint = randomDeviceFp()
            val name = randomName()
            val surname = randomSurname()
            val hash = randomHash()

            //profile registration phone
            val phoneNumber = randomPersistencePhone()

            //profile id after registration
            val profileId = randomId()

            //authenticator response
            val tokens = randomTokens()

            //service request
            val registerByPhoneRequest = ru.zveron.authservice.service.model.RegisterByPhoneRequest(
                fingerprint = fingerprint,
                sessionId = sessionId,
                password = RandomUtils.nextBytes(10),
                name = name,
                surname = surname,
            )

            //context when we continue login flow to registration
            val registrationContext = MobilePhoneRegisterStateContext(
                phoneNumber = phoneNumber,
                fingerprint = fingerprint,
                isChannelVerified = true,
            )

            //request to register new profile
            val registerClientRequest = ru.zveron.authservice.grpc.client.model.RegisterByPhoneRequest(
                name = name,
                phone = PhoneNumber.of(phoneNumber),
                hash = hash,
                surname = surname,
            )

            coEvery {
                flowStorage.getContext(
                    sessionId,
                    MobilePhoneRegisterStateContext::class
                )
            } returns registrationContext

            coEvery { profileServiceClient.registerProfileByPhone(registerClientRequest) } returns RegisterProfileSuccess(
                profileId
            )
            coEvery { argon2PasswordEncoder.encode(any()) } returns hash

            coEvery { authenticator.loginUser(fingerprint, profileId) } returns tokens

            val serviceResponse = service.registerByPhone(registerByPhoneRequest)
            serviceResponse.shouldNotBeNull()
            serviceResponse shouldBe tokens
        }
    }

    @Test
    fun `when register profile request and channel is not verified, then throws exception`(): Unit = runBlocking {
        //given

        //request data
        val sessionId = UUID.randomUUID()
        val fingerprint = randomDeviceFp()
        val name = randomName()
        val surname = randomSurname()

        //profile registration phone
        val phoneNumber = randomPersistencePhone()

        //context when we continue login flow to registration
        val registrationContext = MobilePhoneRegisterStateContext(
            phoneNumber = phoneNumber,
            fingerprint = fingerprint,
            isChannelVerified = false,
        )

        coEvery {
            flowStorage.getContext(
                sessionId,
                MobilePhoneRegisterStateContext::class
            )
        } returns registrationContext

        //service request
        val registerByPhoneRequest = ru.zveron.authservice.service.model.RegisterByPhoneRequest(
            fingerprint = fingerprint,
            sessionId = sessionId,
            password = RandomUtils.nextBytes(10),
            name = name,
            surname = surname,
        )

        assertThrows<ChannelNotVerifiedException> {
            service.registerByPhone(registerByPhoneRequest)
        }
    }

    @Test
    fun `when register profile request and channel is verified but client returns error, then throws exception`(): Unit =
        runBlocking {
            //given

            //request data
            val sessionId = UUID.randomUUID()
            val fingerprint = randomDeviceFp()
            val name = randomName()
            val surname = randomSurname()
            val hash = randomHash()

            //profile registration phone
            val phoneNumber = randomPersistencePhone()

            //context when we continue login flow to registration
            val registrationContext = MobilePhoneRegisterStateContext(
                phoneNumber = phoneNumber,
                fingerprint = fingerprint,
                isChannelVerified = true,
            )

            //service request
            val registerByPhoneRequest = ru.zveron.authservice.service.model.RegisterByPhoneRequest(
                fingerprint = fingerprint,
                sessionId = sessionId,
                password = RandomUtils.nextBytes(10),
                name = name,
                surname = surname
            )

            //request to register new profile
            val registerClientRequest = ru.zveron.authservice.grpc.client.model.RegisterByPhoneRequest(
                name = name,
                phone = PhoneNumber.of(phoneNumber),
                hash = hash,
                surname = surname,
            )

            coEvery {
                flowStorage.getContext(
                    sessionId,
                    MobilePhoneRegisterStateContext::class
                )
            } returns registrationContext

            coEvery { argon2PasswordEncoder.encode(any()) } returns hash

            coEvery { profileServiceClient.registerProfileByPhone(registerClientRequest) } returns RegisterProfileAlreadyExists

            assertThrows<RegistrationException> {
                service.registerByPhone(registerByPhoneRequest)
            }
        }

    @Test
    fun `when register profile request and fingerprints dont match, then throws exception`() {
        return runBlocking {
            //given

            //request data
            val sessionId = UUID.randomUUID()
            val fingerprint = randomDeviceFp()
            val name = randomName()
            val surname = randomSurname()

            //profile registration phone
            val phoneNumber = randomPersistencePhone()

            //service request
            val registerByPhoneRequest = ru.zveron.authservice.service.model.RegisterByPhoneRequest(
                fingerprint = fingerprint,
                sessionId = sessionId,
                password = RandomUtils.nextBytes(10),
                name = name,
                surname = surname,
            )

            //context when we continue login flow to registration
            val registrationContext = MobilePhoneRegisterStateContext(
                phoneNumber = phoneNumber,
                fingerprint = randomDeviceFp(),
                isChannelVerified = true,
            )

            coEvery {
                flowStorage.getContext(
                    sessionId,
                    MobilePhoneRegisterStateContext::class
                )
            } returns registrationContext

            assertThrows<FingerprintException> {
                service.registerByPhone(registerByPhoneRequest)
            }
        }
    }

    @Test
    fun `when register profile request and context not found, then throw exception`() {
        return runBlocking {
            //given

            //request data
            val sessionId = UUID.randomUUID()
            val fingerprint = randomDeviceFp()
            val name = randomName()
            val surname = randomSurname()

            //service request
            val registerByPhoneRequest = ru.zveron.authservice.service.model.RegisterByPhoneRequest(
                fingerprint = fingerprint,
                sessionId = sessionId,
                password = RandomUtils.nextBytes(10),
                name = name,
                surname = surname,
            )

            coEvery {
                flowStorage.getContext(
                    sessionId,
                    MobilePhoneRegisterStateContext::class
                )
            } throws ContextExpiredException()

            assertThrows<ContextExpiredException> {
                service.registerByPhone(registerByPhoneRequest)
            }
        }
    }
}
