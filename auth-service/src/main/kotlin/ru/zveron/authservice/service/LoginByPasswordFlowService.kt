package ru.zveron.authservice.service

import mu.KLogging
import net.logstash.logback.marker.Markers.append
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.stereotype.Service
import ru.zveron.authservice.component.auth.Authenticator
import ru.zveron.authservice.component.jwt.model.MobileTokens
import ru.zveron.authservice.exception.PasswordValidationException
import ru.zveron.authservice.grpc.client.ProfileServiceClient
import ru.zveron.authservice.grpc.client.model.FindProfileUnknownFailure
import ru.zveron.authservice.grpc.client.model.PasswordIsInvalid
import ru.zveron.authservice.grpc.client.model.PasswordIsValid
import ru.zveron.authservice.grpc.client.model.PasswordValidationFailure
import ru.zveron.authservice.grpc.client.model.ProfileFound
import ru.zveron.authservice.grpc.client.model.ProfileNotFound
import ru.zveron.authservice.service.mapper.ServiceMapper.toClientRequest
import ru.zveron.authservice.service.model.LoginByPasswordRequest

@Service
class LoginByPasswordFlowService(
    private val authenticator: Authenticator,
    private val profileClient: ProfileServiceClient,
    private val argon2PasswordEncoder: Argon2PasswordEncoder,
) {

    companion object : KLogging()

    suspend fun loginByPassword(request: LoginByPasswordRequest): MobileTokens {
        val passwordHash = argon2PasswordEncoder.encode(request.password.decodeToString())

        val validatePwdResponse = profileClient.validatePassword(request.toClientRequest(passwordHash))

        logger.debug { "Validating password" }

        when (validatePwdResponse) {
            PasswordIsValid -> {}

            PasswordIsInvalid -> throw PasswordValidationException("Password is invalid")
            is PasswordValidationFailure -> throw PasswordValidationException(
                validatePwdResponse.message,
                validatePwdResponse.status.code,
                validatePwdResponse.metadata
            )
        }

        logger.debug(append("phone", request.loginPhone)) { "Searching for the profile" }

        val findProfileResponse = profileClient.getProfileByPhone(request.loginPhone.toClientPhone())

        val profileId: Long = when (findProfileResponse) {
            is ProfileFound -> findProfileResponse.id

            ProfileNotFound -> error("Password is valid, but account not found after validation for phone=${request.loginPhone}")
            is FindProfileUnknownFailure -> throw PasswordValidationException(
                findProfileResponse.message,
                findProfileResponse.code,
                findProfileResponse.metadata
            )
        }

        logger.debug(append("profileId", profileId)) { "Authenticating" }

        return authenticator.loginUser(profileId = profileId, fingerprint = request.fingerprint)
    }
}
