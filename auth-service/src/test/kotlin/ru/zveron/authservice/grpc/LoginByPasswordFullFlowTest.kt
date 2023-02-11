package ru.zveron.authservice.grpc

import com.google.protobuf.kotlin.toByteString
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import ru.zveron.authservice.component.jwt.JwtDecoder
import ru.zveron.authservice.config.BaseAuthTest
import ru.zveron.authservice.exception.PasswordValidationException
import ru.zveron.authservice.grpc.client.model.PasswordIsInvalid
import ru.zveron.authservice.grpc.client.model.PasswordIsValid
import ru.zveron.authservice.grpc.client.model.ProfileFound
import ru.zveron.authservice.grpc.client.model.ProfileNotFound
import ru.zveron.authservice.grpc.client.model.ValidatePasswordRequest
import ru.zveron.authservice.util.randomApigPhone
import ru.zveron.authservice.util.randomDeviceFp
import ru.zveron.authservice.util.randomId
import ru.zveron.authservice.util.randomName
import ru.zveron.authservice.util.randomPassword
import ru.zveron.authservice.util.randomSurname
import ru.zveron.contract.auth.loginByPasswordRequest

class LoginByPasswordFullFlowTest : BaseAuthTest() {

    @Autowired
    lateinit var controller: AuthLoginController

    @Autowired
    lateinit var argon2PasswordEncoder: Argon2PasswordEncoder

    @Autowired
    lateinit var decoder: JwtDecoder

    @Test
    fun `happy pass test`() = runBlocking {
        //given

        //service request
        val grpcPassword = randomPassword().toByteString()
        val fingerprint = randomDeviceFp()
        val randomApigPhone = randomApigPhone()

        //existing profile data
        val profileId = randomId()
        val name = randomName()
        val surname = randomSurname()

        //login by password request to controller
        val request = loginByPasswordRequest {
            this.password = grpcPassword
            this.deviceFp = fingerprint
            this.phoneNumber = randomApigPhone
        }

        //record request to validate that hash then matches with password
        val requestSlot = slot<ValidatePasswordRequest>()

        //validate password successfully
        coEvery { profileClient.validatePassword(capture(requestSlot)) } returns PasswordIsValid

        //can find profile
        coEvery { profileClient.getAccountByPhone(any()) } returns ProfileFound(profileId, name, surname)

        //when
        val loginByPasswordResponse = controller.loginByPassword(request)

        //tokens belong to profile
        val accessToken = decoder.decodeAccessToken(loginByPasswordResponse.accessToken.token)
        accessToken.profileId shouldBe profileId

        val refreshToken = decoder.decodeRefreshToken(loginByPasswordResponse.refreshToken.token)
        refreshToken.profileId shouldBe profileId

        //called password validation and capture request
        coVerify { profileClient.validatePassword(capture(requestSlot)) }

        //validate that hash can be used to validate same password
        val passwordHash = requestSlot.captured.passwordHash
        val matches = argon2PasswordEncoder.matches(grpcPassword.toByteArray().decodeToString(), passwordHash)
        matches shouldBe true
    }

    @Test
    fun `when password not valid, then `(): Unit = runBlocking {
        //service request
        val grpcPassword = randomPassword().toByteString()
        val fingerprint = randomDeviceFp()
        val randomApigPhone = randomApigPhone()

        val request = loginByPasswordRequest {
            this.password = grpcPassword
            this.deviceFp = fingerprint
            this.phoneNumber = randomApigPhone
        }

        //wrong password == exception
        coEvery { profileClient.validatePassword(any()) } returns PasswordIsInvalid

        assertThrows<PasswordValidationException> {
            controller.loginByPassword(request)
        }
    }

    @Test
    fun `when password is valid, but profile service cant find profile, then throw exception`(): Unit = runBlocking {
        //service request
        val grpcPassword = randomPassword().toByteString()
        val fingerprint = randomDeviceFp()
        val randomApigPhone = randomApigPhone()

        val request = loginByPasswordRequest {
            this.password = grpcPassword
            this.deviceFp = fingerprint
            this.phoneNumber = randomApigPhone
        }

        //password is ok
        coEvery { profileClient.validatePassword(any()) } returns PasswordIsValid

        //but cant find profile afterwards == Illegal state for our system
        coEvery { profileClient.getAccountByPhone(any()) } returns ProfileNotFound

        assertThrows<IllegalStateException> {
            controller.loginByPassword(request)
        }
    }
}
