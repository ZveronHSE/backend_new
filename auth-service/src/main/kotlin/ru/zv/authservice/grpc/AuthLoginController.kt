package ru.zv.authservice.grpc

import net.devh.boot.grpc.server.service.GrpcService
import ru.zv.authservice.persistence.model.MOBILE_PHONE_REGISTER_ALIAS
import ru.zv.authservice.service.LoginByPhoneFlowService
import ru.zveron.contract.auth.AuthServiceGrpcKt
import ru.zveron.contract.auth.PhoneLoginInitRequest
import ru.zveron.contract.auth.PhoneLoginInitResponse
import ru.zveron.contract.auth.PhoneLoginVerifyRequest
import ru.zveron.contract.auth.PhoneLoginVerifyResponse
import ru.zveron.contract.auth.phoneLoginInitResponse
import ru.zveron.contract.auth.phoneLoginVerifyResponse

@GrpcService
class AuthLoginController(
    private val loginFlowService: LoginByPhoneFlowService,
) : AuthServiceGrpcKt.AuthServiceCoroutineImplBase() {


    override suspend fun phoneLoginInit(request: PhoneLoginInitRequest): PhoneLoginInitResponse {
        val sessionId = loginFlowService.init(request.toServiceRequest())
        return phoneLoginInitResponse {
            this.sessionId = sessionId.toString()
        }
    }

    override suspend fun phoneLoginVerify(request: PhoneLoginVerifyRequest): PhoneLoginVerifyResponse {
        val serviceResponse = loginFlowService.verify(request.toServiceRequest())
        return phoneLoginVerifyResponse {
            this.sessionId = serviceResponse.sessionId.toString()
            this.mobileToken = serviceResponse.tokens.toGrpcToken()
            this.isNewUser = serviceResponse.isNewUser
        }
    }
}
