package ru.zveron.authservice.grpc

import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.authservice.component.auth.Authenticator
import ru.zveron.authservice.grpc.GrpcMapper.toGrpcContract
import ru.zveron.authservice.grpc.GrpcMapper.toGrpcToken
import ru.zveron.authservice.grpc.GrpcMapper.toServiceRequest
import ru.zveron.authservice.service.LoginByPasswordFlowService
import ru.zveron.authservice.service.LoginByPhoneFlowService
import ru.zveron.contract.auth.AuthServiceGrpcKt
import ru.zveron.contract.auth.IssueNewTokensRequest
import ru.zveron.contract.auth.LoginByPasswordRequest
import ru.zveron.contract.auth.MobileToken
import ru.zveron.contract.auth.PhoneLoginInitRequest
import ru.zveron.contract.auth.PhoneLoginInitResponse
import ru.zveron.contract.auth.PhoneLoginVerifyRequest
import ru.zveron.contract.auth.PhoneLoginVerifyResponse
import ru.zveron.contract.auth.ProfileDto
import ru.zveron.contract.auth.VerifyMobileTokenRequest
import ru.zveron.contract.auth.phoneLoginInitResponse
import ru.zveron.contract.auth.profileDto

@GrpcService
class AuthLoginController(
    private val loginFlowService: LoginByPhoneFlowService,
    private val authenticator: Authenticator,
    private val loginByPasswordFlowService: LoginByPasswordFlowService,
) : AuthServiceGrpcKt.AuthServiceCoroutineImplBase() {

    override suspend fun phoneLoginInit(request: PhoneLoginInitRequest): PhoneLoginInitResponse {
        val sessionId = loginFlowService.init(request.toServiceRequest())
        return phoneLoginInitResponse {
            this.sessionId = sessionId.toString()
        }
    }

    override suspend fun phoneLoginVerify(request: PhoneLoginVerifyRequest): PhoneLoginVerifyResponse {
        val serviceResponse = loginFlowService.verify(request.toServiceRequest())
        return serviceResponse.toGrpcContract()
    }

    override suspend fun verifyToken(request: VerifyMobileTokenRequest): ProfileDto =
        profileDto {
            this.id = authenticator.validateAccessToken(request.accessToken)
        }

    override suspend fun issueNewTokens(request: IssueNewTokensRequest): MobileToken {
        val mobileTokens = authenticator.refreshMobileSession(request.toServiceRequest())
        return mobileTokens.toGrpcToken()
    }

    override suspend fun loginByPassword(request: LoginByPasswordRequest): MobileToken {
        val response = loginByPasswordFlowService.loginByPassword(request.toServiceRequest())
        return response.toGrpcToken()
    }
}
