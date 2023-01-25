package ru.zv.authservice.grpc

import net.devh.boot.grpc.server.service.GrpcService
import ru.zv.authservice.service.LoginByPhoneFlowService
import ru.zveron.contract.AuthServiceGrpcKt
import ru.zveron.contract.PhoneLoginInitRequest
import ru.zveron.contract.PhoneLoginInitResponse
import ru.zveron.contract.PhoneLoginVerifyRequest
import ru.zveron.contract.PhoneLoginVerifyResponse
import ru.zveron.contract.phoneLoginInitResponse
import ru.zveron.contract.phoneLoginVerifyResponse

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
            this.authFlowType = serviceResponse.flowType
            this.mobileToken = serviceResponse.tokens.toGrpcToken()
        }
    }
}
