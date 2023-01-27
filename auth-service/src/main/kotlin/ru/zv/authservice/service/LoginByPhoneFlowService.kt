package ru.zv.authservice.service

import io.grpc.Status
import mu.KLogging
import org.springframework.stereotype.Service
import ru.zv.authservice.exceptions.CodeValidatedException
import ru.zv.authservice.exceptions.FingerprintException
import ru.zv.authservice.exceptions.NotifierClientException
import ru.zv.authservice.exceptions.WrongCodeException
import ru.zv.authservice.grpc.client.ProfileServiceClient
import ru.zv.authservice.grpc.client.dto.ProfileFound
import ru.zv.authservice.grpc.client.dto.ProfileNotFound
import ru.zv.authservice.grpc.client.dto.ProfileUnknownFailure
import ru.zv.authservice.persistence.FlowStateStorage
import ru.zv.authservice.persistence.model.MobilePhoneLoginStateContext
import ru.zv.authservice.persistence.model.MobilePhoneRegisterStateContext
import ru.zv.authservice.service.dto.LoginByPhoneInitRequest
import ru.zv.authservice.service.dto.LoginByPhoneVerifyRequest
import ru.zv.authservice.service.dto.LoginByPhoneVerifyResponse
import ru.zv.authservice.service.dto.ProfileTokenData
import ru.zv.authservice.service.dto.toClientRequest
import ru.zv.authservice.service.dto.toContext
import ru.zv.authservice.webclient.NotifierClient
import ru.zv.authservice.webclient.NotifierFailure
import ru.zv.authservice.webclient.NotifierSuccess
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
            deviceFp = request.deviceFp,
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

    suspend fun verify(request: LoginByPhoneVerifyRequest): LoginByPhoneVerifyResponse {
        val loginCtx = flowStateStorage.getContext<MobilePhoneLoginStateContext>(request.sessionId)

        validateContext(loginCtx, request.deviceFp)

        val updatedCtx = validateCodeAndUpdateContext(loginCtx, request.code, request.sessionId)

        val profileResponse = profileClient.getAccountByPhone(updatedCtx.phoneNumber)

        val profileData: ProfileTokenData? = when (profileResponse) {
            is ProfileFound -> ProfileTokenData(profileResponse.id, profileResponse.name, profileResponse.surname)
            is ProfileNotFound -> null
            is ProfileUnknownFailure -> throw NotifierClientException(
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
                phoneNumber = loginCtx.phoneNumber,
                deviceFp = loginCtx.deviceFp,
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
            throw FingerprintException("Device fp does not match", Status.Code.UNAUTHENTICATED)
        }
    }

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
