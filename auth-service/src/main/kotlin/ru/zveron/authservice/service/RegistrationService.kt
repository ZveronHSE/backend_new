package ru.zveron.authservice.service

import io.grpc.Status
import mu.KLogging
import net.logstash.logback.marker.Markers.append
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.stereotype.Service
import ru.zveron.authservice.component.auth.Authenticator
import ru.zveron.authservice.component.jwt.model.MobileTokens
import ru.zveron.authservice.exception.ChannelNotVerifiedException
import ru.zveron.authservice.exception.ContextExpiredException
import ru.zveron.authservice.exception.FingerprintException
import ru.zveron.authservice.exception.RegistrationException
import ru.zveron.authservice.grpc.client.ProfileServiceClient
import ru.zveron.authservice.grpc.client.model.PhoneNumber
import ru.zveron.authservice.grpc.client.model.RegisterProfileAlreadyExists
import ru.zveron.authservice.grpc.client.model.RegisterProfileFailure
import ru.zveron.authservice.grpc.client.model.RegisterProfileSuccess
import ru.zveron.authservice.persistence.FlowStateStorage
import ru.zveron.authservice.persistence.model.MobilePhoneRegisterStateContext

@Service
class RegistrationService(
    private val flowStateStorage: FlowStateStorage,
    private val profileServiceClient: ProfileServiceClient,
    private val authenticator: Authenticator,
    private val argon2Encoder: Argon2PasswordEncoder,
) {

    companion object : KLogging()

    /**
     * @throws [RegistrationException]
     * @throws [ChannelNotVerifiedException]
     * @throws [FingerprintException]
     * @throws [ContextExpiredException]
     *
     */
    suspend fun registerByPhone(request: ru.zveron.authservice.service.model.RegisterByPhoneRequest): MobileTokens {
        val registrationContext = flowStateStorage.getContext(request.sessionId, MobilePhoneRegisterStateContext::class)

        if (registrationContext.fingerprint != request.fingerprint) {
            throw FingerprintException()
        }

        if (!registrationContext.isChannelVerified) {
            throw ChannelNotVerifiedException()
        }

        val hash = argon2Encoder.encode(request.password.decodeToString())

        val response = profileServiceClient.registerProfileByPhone(
            ru.zveron.authservice.grpc.client.model.RegisterByPhoneRequest(
                request.name,
                request.surname,
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
        }

        logger.debug(append("profileId", profileId)) { "Profile creation succeeded" }

        return authenticator.loginUser(request.fingerprint, profileId)
    }
}
