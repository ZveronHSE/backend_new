package ru.zveron.authservice.grpc

import com.google.protobuf.Empty
import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.authservice.component.auth.Authenticator
import ru.zveron.authservice.service.LoginByPhoneFlowService
import ru.zveron.contract.auth.AuthServiceGrpcKt
import ru.zveron.contract.auth.IssueNewTokensRequest
import ru.zveron.contract.auth.MobileToken
import ru.zveron.contract.auth.PhoneLoginInitRequest
import ru.zveron.contract.auth.PhoneLoginInitResponse
import ru.zveron.contract.auth.PhoneLoginVerifyRequest
import ru.zveron.contract.auth.PhoneLoginVerifyResponse
import ru.zveron.contract.auth.VerifyMobileTokenRequest
import ru.zveron.contract.auth.phoneLoginInitResponse
import ru.zveron.contract.auth.phoneLoginVerifyResponse

@GrpcService
class AuthLoginController(
    private val loginFlowService: LoginByPhoneFlowService,
    private val authenticator: Authenticator,
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

    override suspend fun verifyToken(request: VerifyMobileTokenRequest): Empty {
        authenticator.validateAccessToken(request.accessToken)
        return Empty.getDefaultInstance()
    }

    override suspend fun issueNewTokens(request: IssueNewTokensRequest): MobileToken {
        val mobileTokens = authenticator.refreshMobileSession(request.toServiceRequest())
        return mobileTokens.toGrpcToken()
    }
}
