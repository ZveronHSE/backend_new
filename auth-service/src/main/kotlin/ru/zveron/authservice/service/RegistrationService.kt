package ru.zveron.authservice.service

import io.grpc.Status
import io.grpc.StatusException
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.stereotype.Service
import ru.zveron.authservice.component.auth.Authenticator
import ru.zveron.authservice.component.jwt.model.MobileTokens
import ru.zveron.authservice.exception.ChannelNotValidatedException
import ru.zveron.authservice.exception.ContextExpiredException
import ru.zveron.authservice.exception.FingerprintException
import ru.zveron.authservice.exception.RegistrationException
import ru.zveron.authservice.grpc.client.PhoneNumber
import ru.zveron.authservice.grpc.client.ProfileServiceClient
import ru.zveron.authservice.grpc.client.RegisterProfileByPhone
import ru.zveron.authservice.grpc.client.model.RegisterProfileAlreadyExists
import ru.zveron.authservice.grpc.client.model.RegisterProfileFailure
import ru.zveron.authservice.grpc.client.model.RegisterProfileSuccess
import ru.zveron.authservice.persistence.FlowStateStorage
import ru.zveron.authservice.persistence.model.MobilePhoneRegisterStateContext
import ru.zveron.authservice.service.model.RegisterByPhoneRequest

@Service
class RegistrationService(
    private val flowStateStorage: FlowStateStorage,
    private val profileServiceClient: ProfileServiceClient,
    private val authenticator: Authenticator,
    private val argon2Encoder: Argon2PasswordEncoder,
) {

    /**
     * @throws [RegistrationException]
     * @throws [ChannelNotValidatedException]
     * @throws [FingerprintException]
     * @throws [ContextExpiredException]
     *
     */
    suspend fun registerByPhone(request: RegisterByPhoneRequest): MobileTokens {
        val registrationContext = flowStateStorage.getContext(request.sessionId, MobilePhoneRegisterStateContext::class)

        if (registrationContext.fingerprint != request.fingerprint) {
            throw FingerprintException()
        }

        if (!registrationContext.isChannelVerified) {
            throw ChannelNotValidatedException()
        }

        val hash = argon2Encoder.encode(request.password.decodeToString())

        val response = profileServiceClient.registerProfileByPhone(
            RegisterProfileByPhone(
                request.name,
                PhoneNumber.of(registrationContext.phoneNumber),
                hash,
            )
        )

        val profileId: Long = when (response) {
            is RegisterProfileSuccess -> response.profileId
            is RegisterProfileAlreadyExists -> throw RegistrationException(code = Status.Code.ALREADY_EXISTS)
            is RegisterProfileFailure -> throw RegistrationException(
                response.message,
                response.code.code,
                response.metadata
            )

            else -> throw StatusException(Status.UNIMPLEMENTED)
        }

        return authenticator.loginUser(request.fingerprint, profileId)
    }
}
