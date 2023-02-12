package ru.zveron.authservice.service

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import ru.zveron.authservice.component.auth.Authenticator
import ru.zveron.authservice.exception.PasswordValidationException
import ru.zveron.authservice.grpc.client.ProfileServiceClient
import ru.zveron.authservice.grpc.client.model.PasswordIsInvalid
import ru.zveron.authservice.grpc.client.model.PasswordIsValid
import ru.zveron.authservice.grpc.client.model.ProfileFound
import ru.zveron.authservice.grpc.client.model.ProfileNotFound
import ru.zveron.authservice.grpc.client.model.ValidatePasswordRequest
import ru.zveron.authservice.service.model.LoginByPasswordRequest
import ru.zveron.authservice.util.randomDeviceFp
import ru.zveron.authservice.util.randomHash
import ru.zveron.authservice.util.randomId
import ru.zveron.authservice.util.randomName
import ru.zveron.authservice.util.randomPassword
import ru.zveron.authservice.util.randomPhoneNumber
import ru.zveron.authservice.util.randomSurname
import ru.zveron.authservice.util.randomTokens

class LoginByPasswordFlowServiceTest {

    private val authenticator = mockk<Authenticator>()
    private val profileServiceClient = mockk<ProfileServiceClient>()
    private val argon2PasswordEncoder = mockk<Argon2PasswordEncoder>()

    private val service = LoginByPasswordFlowService(
        authenticator = authenticator,
        profileClient = profileServiceClient,
        argon2PasswordEncoder = argon2PasswordEncoder,
    )

    @Test
    fun `happy pass test`() {
        //login by pwd request data
        val loginPhone = randomPhoneNumber()
        val password = randomPassword()
        val fingerprint = randomDeviceFp()

        val passwordHash = randomHash()

        //service request
        val request = LoginByPasswordRequest(
            loginPhone = loginPhone,
            password = password,
            fingerprint = fingerprint,
        )

        //the profile we found id
        val profileId = randomId()

        //profile client validate pwd
        val validatePwdRequest =
            ValidatePasswordRequest(phoneNumber = loginPhone.toClientPhone(), passwordHash = passwordHash)

        coEvery { profileServiceClient.validatePassword(validatePwdRequest) } returns PasswordIsValid
        coEvery { profileServiceClient.getProfileByPhone(loginPhone.toClientPhone()) } returns ProfileFound(
            profileId,
            randomName(),
            randomSurname()
        )
        coEvery { authenticator.loginUser(fingerprint, profileId) } returns randomTokens()
        coEvery { argon2PasswordEncoder.encode(any()) } returns passwordHash

        //should not throw any exceptions
        assertDoesNotThrow {
            runBlocking {
                service.loginByPassword(request)
            }
        }
    }

    @Test
    fun `when password validation fails in client, throws exception`() {
        //login by pwd request data
        val loginPhone = randomPhoneNumber()
        val password = randomPassword()
        val fingerprint = randomDeviceFp()

        val passwordHash = randomHash()

        //service request
        val request = LoginByPasswordRequest(
            loginPhone = loginPhone,
            password = password,
            fingerprint = fingerprint,
        )

        //profile client validate pwd
        val validatePwdRequest =
            ValidatePasswordRequest(phoneNumber = loginPhone.toClientPhone(), passwordHash = passwordHash)

        coEvery { profileServiceClient.validatePassword(validatePwdRequest) } returns PasswordIsInvalid
        coEvery { argon2PasswordEncoder.encode(any()) } returns passwordHash

        //should not throw any exceptions
        shouldThrow<PasswordValidationException> {
            runBlocking {
                service.loginByPassword(request)
            }
        }
    }

    @Test
    fun `when profile not found after validation succeeds, then throws exception`() {
        //login by pwd request data
        val loginPhone = randomPhoneNumber()
        val password = randomPassword()
        val fingerprint = randomDeviceFp()

        val passwordHash = randomHash()

        //service request
        val request = LoginByPasswordRequest(
            loginPhone = loginPhone,
            password = password,
            fingerprint = fingerprint,
        )

        //profile client validate pwd
        val validatePwdRequest =
            ValidatePasswordRequest(phoneNumber = loginPhone.toClientPhone(), passwordHash = passwordHash)

        coEvery { profileServiceClient.validatePassword(validatePwdRequest) } returns PasswordIsValid
        coEvery { profileServiceClient.getProfileByPhone(loginPhone.toClientPhone()) } returns ProfileNotFound
        coEvery { argon2PasswordEncoder.encode(any()) } returns passwordHash

        //should not throw any exceptions
        shouldThrow<IllegalStateException> {
            runBlocking {
                service.loginByPassword(request)
            }
        }
    }
}
