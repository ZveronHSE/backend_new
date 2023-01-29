package ru.zveron.authservice.service

import io.grpc.Status
import mu.KLogging
import org.springframework.stereotype.Service
import ru.zveron.authservice.grpc.client.ProfileServiceClient
import ru.zveron.authservice.grpc.client.dto.ProfileFound
import ru.zveron.authservice.grpc.client.dto.ProfileNotFound
import ru.zveron.authservice.grpc.client.dto.ProfileUnknownFailure
import ru.zveron.authservice.persistence.FlowStateStorage
import ru.zveron.authservice.persistence.model.MobilePhoneLoginStateContext
import ru.zveron.authservice.persistence.model.MobilePhoneRegisterStateContext
import ru.zveron.authservice.service.ServiceMapper.toClientRequest
import ru.zveron.authservice.service.ServiceMapper.toContext
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
) {

    companion object : KLogging()

    suspend fun init(request: LoginByPhoneInitRequest): UUID {
        val phoneVerificationCtx = MobilePhoneLoginStateContext(
            phoneNumber = request.phoneNumber.toContext(),
            deviceFp = request.deviceFingerprint,
        )

        val verificationContext =
            when (val clientResponse = notifierClient.initializeVerification(request.toClientRequest())) {
                is NotifierFailure -> {
                    logger.error { "An error produced in the client, response is $clientResponse" }
                    throw ru.zveron.authservice.exception.NotifierClientException()
                }

                is NotifierSuccess -> phoneVerificationCtx.copy(code = clientResponse.verificationCode)
            }

        return flowStateStorage.createContext(verificationContext)
    }

    suspend fun verify(request: LoginByPhoneVerifyRequest): LoginByPhoneVerifyResponse {
        val loginCtx = flowStateStorage.getContext<MobilePhoneLoginStateContext>(request.sessionId)

        validateContext(loginCtx, request.deviceFingerprint)

        val updatedCtx = validateCodeAndUpdateContext(loginCtx, request.code, request.sessionId)

        val profileResponse = profileClient.getAccountByPhone(updatedCtx.phoneNumber)

        val profileData: ProfileTokenData? = when (profileResponse) {
            is ProfileFound -> ProfileTokenData(profileResponse.id, profileResponse.name, profileResponse.surname)
            is ProfileNotFound -> null
            is ProfileUnknownFailure -> throw ru.zveron.authservice.exception.NotifierClientException(
                profileResponse.message ?: "no message",
                profileResponse.code
            )
        }

        return profileData?.let {
            LoginByPhoneVerifyResponse.login(
                request.sessionId,
                accessToken = "mock-access-token-${request.sessionId}",
                refreshToken = "mock-refresh-token-${request.sessionId}",
            )
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
            throw ru.zveron.authservice.exception.CodeValidatedException()
        }
        if (loginCtx.deviceFp != requestDeviceFp) {
            throw ru.zveron.authservice.exception.FingerprintException(
                "Device fp does not match",
                Status.Code.UNAUTHENTICATED
            )
        }
    }

    private suspend fun validateCodeAndUpdateContext(
        loginCtx: MobilePhoneLoginStateContext,
        code: String,
        sessionId: UUID,
    ): MobilePhoneLoginStateContext {
        val updatedCtx = loginCtx.copy(codeAttempts = loginCtx.codeAttempts.inc(), lastAttemptAt = Instant.now())

        if (loginCtx.code != code) {
            throw ru.zveron.authservice.exception.WrongCodeException("Wrong code", Status.Code.INVALID_ARGUMENT)
        }

        return flowStateStorage.updateContext(sessionId = sessionId, context = updatedCtx.copy(isVerified = true))
    }
}
