package ru.zveron.authservice.service

import io.grpc.Status
import mu.KLogging
import org.springframework.stereotype.Service
import ru.zveron.authservice.component.auth.Authenticator
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
    private val authenticator: Authenticator,
) {
    companion object : KLogging()

    /**
     * throws [NotifierClientException]
     */
    suspend fun init(request: LoginByPhoneInitRequest): UUID {
        val phoneVerificationCtx = MobilePhoneLoginStateContext(
            phoneNumber = request.phoneNumber.toContext(),
            fingerprint = request.fingerprint,
        )

        val notifierResponse = notifierClient.initializeVerification(request.toClientRequest())

        val verificationCtxWithCode =
            when (notifierResponse) {
                is NotifierFailure -> {
                    logger.error { "An error produced in the client, response is $notifierResponse" }
                    throw NotifierClientException()
                }

                is NotifierSuccess -> phoneVerificationCtx.copy(code = notifierResponse.verificationCode)
            }

        return flowStateStorage.createContext(verificationCtxWithCode)
    }

    /**
     * throws [ContextExpiredException]
     * throws [WrongCodeException]
     * throws [NotifierClientException]
     * throws [CodeValidatedException]
     * throws [FingerprintException]
     */
    suspend fun verify(request: LoginByPhoneVerifyRequest): LoginByPhoneVerifyResponse {
        val phoneVerificationCtx = flowStateStorage.getContext<MobilePhoneLoginStateContext>(request.sessionId)

        validateContext(phoneVerificationCtx, request.fingerprint)

        val updatedCtx = validateCodeAndUpdateContext(phoneVerificationCtx, request.code, request.sessionId)

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
            val tokens = authenticator.loginUser(request.fingerprint, it.id)
            LoginByPhoneVerifyResponse.login(tokens)
        } ?: flowStateStorage.createContext(
            MobilePhoneRegisterStateContext(
                phoneNumber = updatedCtx.phoneNumber,
                deviceFp = updatedCtx.fingerprint,
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
        if (loginCtx.fingerprint != requestDeviceFp) {
            throw FingerprintException(
                "Device fingerprint does not match",
                Status.Code.UNAUTHENTICATED
            )
        }
    }

    /**
     * throws [WrongCodeException]
     * throws [ContextExpiredException]
     */
    private suspend fun validateCodeAndUpdateContext(
        verifyMobileCtx: MobilePhoneLoginStateContext,
        code: String,
        sessionId: UUID,
    ): MobilePhoneLoginStateContext {
        val updatedCtx =
            verifyMobileCtx.copy(codeAttempts = verifyMobileCtx.codeAttempts.inc(), lastAttemptAt = Instant.now())

        if (verifyMobileCtx.code != code) {
            throw WrongCodeException()
        }

        return flowStateStorage.updateContext(sessionId = sessionId, context = updatedCtx.copy(isVerified = true))
    }
}
