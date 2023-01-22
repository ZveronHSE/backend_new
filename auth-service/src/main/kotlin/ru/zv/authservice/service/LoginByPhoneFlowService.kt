package ru.zv.authservice.service

import io.grpc.Status
import mu.KLogging
import org.springframework.stereotype.Service
import ru.zv.authservice.grpc.ProfileFound
import ru.zv.authservice.grpc.ProfileNotFound
import ru.zv.authservice.grpc.ProfileServiceClient
import ru.zv.authservice.grpc.ProfileUnknownFailure
import ru.zv.authservice.persistence.FlowStateStorage
import ru.zv.authservice.persistence.model.MobilePhoneLoginFlowContext
import ru.zv.authservice.service.dto.LoginByPhoneInitRequest
import ru.zv.authservice.service.dto.LoginByPhoneVerifyRequest
import ru.zv.authservice.service.dto.LoginByPhoneVerifyResponse
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
        val phoneVerificationCtx = MobilePhoneLoginFlowContext(
            phoneNumber = request.phoneNumber.toContext(),
            deviceFp = request.deviceFp,
        )

        val sessionId = flowStateStorage.createContext(phoneVerificationCtx)

        when (val clientResponse = notifierClient.initializeVerification(request.toClientRequest())) {
            is NotifierFailure -> {
                logger.debug { "An error produced in the client, response is $clientResponse" }
                throw AuthException(clientResponse.message, Status.Code.INTERNAL)
            }

            is NotifierSuccess -> flowStateStorage.updateContext(
                sessionId,
                phoneVerificationCtx.copy(code = clientResponse.verificationCode)
            )
        }

        return sessionId
    }

    suspend fun verify(request: LoginByPhoneVerifyRequest): LoginByPhoneVerifyResponse {
        val loginCtx = flowStateStorage.getContext(request.sessionId, MobilePhoneLoginFlowContext::class)
        val updatedCtx = loginCtx.copy(codeAttempts = loginCtx.codeAttempts.inc(), lastAttemptAt = Instant.now())

        if (loginCtx.deviceFp != request.deviceFp) {
            throw AuthException("Device fp does not match", Status.Code.UNAUTHENTICATED)
        }
        if (loginCtx.code != request.code) {
            throw AuthException("Wrong code", Status.Code.INVALID_ARGUMENT)
        }

        val validatedCtx =
            flowStateStorage.updateContext(sessionId = request.sessionId, context = updatedCtx.copy(isVerified = true))

        val profileResponse = profileClient.getAccountByPhone(validatedCtx.phoneNumber)

        val profileData: ProfileTokenData? = when (profileResponse) {
            is ProfileFound -> ProfileTokenData(profileResponse.id, profileResponse.name, profileResponse.surname)
            is ProfileNotFound -> null
            is ProfileUnknownFailure -> throw AuthException(
                message = profileResponse.message,
                code = profileResponse.code
            )
        }

        return profileData?.let {
            LoginByPhoneVerifyResponse.login(
                UUID.randomUUID(),
                accessToken = UUID.randomUUID().toString(),
                refreshToken = UUID.randomUUID().toString()
            )
        } ?: LoginByPhoneVerifyResponse.register(request.sessionId)
    }
}

data class ProfileTokenData(
    val id: Long,
    val name: String,
    val lastname: String,
)
