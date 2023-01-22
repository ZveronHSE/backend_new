package ru.zv.authservice.grpc

import net.devh.boot.grpc.server.service.GrpcService
import ru.zv.authservice.service.LoginByPhoneFlowService
import ru.zv.authservice.service.dto.JwtMobileTokens
import ru.zv.authservice.service.dto.LoginByPhoneInitRequest
import ru.zv.authservice.service.dto.LoginByPhoneVerifyRequest
import ru.zv.authservice.util.PhoneNumberParser.stringToServicePhone
import ru.zveron.contract.AuthServiceGrpcKt
import ru.zveron.contract.MobileToken
import ru.zveron.contract.PhoneLoginInitRequest
import ru.zveron.contract.PhoneLoginInitResponse
import ru.zveron.contract.PhoneLoginVerifyRequest
import ru.zveron.contract.PhoneLoginVerifyResponse
import ru.zveron.contract.mobileToken
import ru.zveron.contract.phoneLoginInitResponse
import ru.zveron.contract.phoneLoginVerifyResponse
import java.util.UUID

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

fun PhoneLoginInitRequest.toServiceRequest() =
    LoginByPhoneInitRequest(phoneNumber = stringToServicePhone(this.phoneNumber), this.deviceFp)

fun PhoneLoginVerifyRequest.toServiceRequest() =
    LoginByPhoneVerifyRequest(code = this.code, sessionId = UUID.fromString(this.sessionId), deviceFp = this.deviceFp)

fun JwtMobileTokens.toGrpcToken(): MobileToken {
    val refreshToken = this.refreshToken
    val accessToken = this.accessToken
    return mobileToken {
        this.refreshToken = refreshToken
        this.accessToken = accessToken
    }
}