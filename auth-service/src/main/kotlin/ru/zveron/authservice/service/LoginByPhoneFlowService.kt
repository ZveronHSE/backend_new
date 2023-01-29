package ru.zveron.authservice.service

import io.grpc.Status
import mu.KLogging
import org.springframework.stereotype.Service
import ru.zveron.authservice.exception.CodeValidatedException
import ru.zveron.authservice.exception.ContextExpiredException
import ru.zveron.authservice.exception.FingerprintException
import ru.zveron.authservice.exception.NotifierClientException
import ru.zveron.authservice.exception.WrongCodeException
import ru.zveron.authservice.grpc.client.ProfileServiceClient
import ru.zveron.authservice.grpc.client.model.ProfileFound
import ru.zveron.authservice.grpc.client.model.ProfileNotFound
import ru.zveron.authservice.grpc.client.model.ProfileUnknownFailure
import ru.zveron.authservice.persistence.FlowStateStorage
import ru.zveron.authservice.persistence.model.MobilePhoneLoginStateContext
import ru.zveron.authservice.persistence.model.MobilePhoneRegisterStateContext
import ru.zveron.authservice.service.ServiceMapper.toClientRequest
import ru.zveron.authservice.service.ServiceMapper.toContext
import ru.zveron.authservice.service.ServiceMapper.toProfileClientRequest
import ru.zveron.authservice.service.model.LoginByPhoneInitRequest
import ru.zveron.authservice.service.model.LoginByPhoneVerifyRequest
import ru.zveron.authservice.service.model.LoginByPhoneVerifyResponse
import ru.zveron.authservice.service.model.ProfileTokenData
import ru.zveron.authservice.webclient.NotifierClient
import ru.zveron.authservice.webclient.NotifierFailure
import ru.zveron.authservice.webclient.NotifierSuccess
import java.time.Instant
import java.util.UUID

@Service
class LoginByPhoneFlowService(
    private val notifierClient: NotifierClient,
    private val flowStateStorage: FlowStateStorage,
    private val profileClient: ProfileServiceClient,
    private val authenticator: ru.zveron.authservice.component.auth.Authenticator,
) {

    companion object : KLogging()

    /**
     * throws [NotifierClientException]
     */
    suspend fun init(request: LoginByPhoneInitRequest): UUID {
        val phoneVerificationCtx = MobilePhoneLoginStateContext(
            phoneNumber = request.phoneNumber.toContext(),
            deviceFp = request.deviceFingerprint,
        )

        val verificationContext =
            when (val clientResponse = notifierClient.initializeVerification(request.toClientRequest())) {
                is NotifierFailure -> {
                    logger.error { "An error produced in the client, response is $clientResponse" }
                    throw NotifierClientException()
                }

                is NotifierSuccess -> phoneVerificationCtx.copy(code = clientResponse.verificationCode)
            }

        return flowStateStorage.createContext(verificationContext)
    }

    /**
     * throws [ContextExpiredException]
     * throws [WrongCodeException]
     * throws [NotifierClientException]
     * throws [CodeValidatedException]
     * throws [FingerprintException]
     */
    suspend fun verify(request: LoginByPhoneVerifyRequest): LoginByPhoneVerifyResponse {
        val loginCtx = flowStateStorage.getContext<MobilePhoneLoginStateContext>(request.sessionId)

        validateContext(loginCtx, request.deviceFingerprint)

        val updatedCtx = validateCodeAndUpdateContext(loginCtx, request.code, request.sessionId)

        val profileResponse = profileClient.getAccountByPhone(updatedCtx.phoneNumber.toProfileClientRequest())

        val profileData: ProfileTokenData? = when (profileResponse) {
            is ProfileFound -> ProfileTokenData(profileResponse.id, profileResponse.name, profileResponse.surname)
            is ProfileNotFound -> null
            is ProfileUnknownFailure -> throw NotifierClientException(
                profileResponse.message ?: "no message",
                profileResponse.code
            )
        }

        return profileData?.let {
            val tokens = authenticator.loginUser(request.deviceFingerprint, it.id)
            LoginByPhoneVerifyResponse.login(request.sessionId, tokens.accessToken.token, tokens.refreshToken.token)
        } ?: flowStateStorage.createContext(
            MobilePhoneRegisterStateContext(
                phoneNumber = updatedCtx.phoneNumber,
                deviceFp = updatedCtx.deviceFp,
                isChannelVerified = updatedCtx.isVerified,
            )
        ).let {
            LoginByPhoneVerifyResponse.registration(it)
        }
    }

    private fun validateContext(loginCtx: MobilePhoneLoginStateContext, requestDeviceFp: String) {
        if (loginCtx.isVerified) {
            throw CodeValidatedException()
        }
        if (loginCtx.deviceFp != requestDeviceFp) {
            throw FingerprintException(
                "Device fp does not match",
                Status.Code.UNAUTHENTICATED
            )
        }
    }

    /**
     * throws [WrongCodeException]
     * throws [ContextExpiredException]
     */
    private suspend fun validateCodeAndUpdateContext(
        loginCtx: MobilePhoneLoginStateContext,
        code: String,
        sessionId: UUID,
    ): MobilePhoneLoginStateContext {
        val updatedCtx = loginCtx.copy(codeAttempts = loginCtx.codeAttempts.inc(), lastAttemptAt = Instant.now())

        if (loginCtx.code != code) {
            throw WrongCodeException("Wrong code", Status.Code.INVALID_ARGUMENT)
        }

        return flowStateStorage.updateContext(sessionId = sessionId, context = updatedCtx.copy(isVerified = true))
    }
}
