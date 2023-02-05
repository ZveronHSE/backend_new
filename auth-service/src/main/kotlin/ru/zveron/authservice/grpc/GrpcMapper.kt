package ru.zveron.authservice.grpc

import com.google.protobuf.timestamp
import ru.zveron.authservice.component.auth.model.RefreshMobileSessionRequest
import ru.zveron.authservice.component.jwt.model.AccessToken
import ru.zveron.authservice.component.jwt.model.MobileTokens
import ru.zveron.authservice.component.jwt.model.RefreshToken
import ru.zveron.authservice.service.model.JwtMobileTokens
import ru.zveron.authservice.service.model.LoginByPhoneInitRequest
import ru.zveron.authservice.service.model.LoginByPhoneVerifyRequest
import ru.zveron.authservice.service.model.LoginByPhoneVerifyResponse
import ru.zveron.authservice.util.PhoneNumberParser
import ru.zveron.contract.auth.IssueNewTokensRequest
import ru.zveron.contract.auth.MobileToken
import ru.zveron.contract.auth.PhoneLoginInitRequest
import ru.zveron.contract.auth.PhoneLoginVerifyRequest
import ru.zveron.contract.auth.TimedToken
import ru.zveron.contract.auth.mobileToken
import ru.zveron.contract.auth.phoneLoginVerifyResponse
import ru.zveron.contract.auth.timedToken
import java.time.Instant
import java.util.UUID

object GrpcMapper {
    fun PhoneLoginInitRequest.toServiceRequest() =
        LoginByPhoneInitRequest(phoneNumber = PhoneNumberParser.stringToServicePhone(this.phoneNumber), this.deviceFp)

    fun PhoneLoginVerifyRequest.toServiceRequest() =
        LoginByPhoneVerifyRequest(
            code = this.code,
            sessionId = UUID.fromString(this.sessionId),
            fingerprint = this.deviceFp
        )

    fun JwtMobileTokens.toGrpcToken(): MobileToken = mobileToken {
        this.accessToken = timedToken {
            this.token = this@toGrpcToken.accessToken
            this.expiration = this@toGrpcToken.accessExpiration.toTimeStamp()
        }
        this.refreshToken = timedToken {
            this.token = this@toGrpcToken.refreshToken
            this.expiration = this@toGrpcToken.refreshExpiration.toTimeStamp()
        }
    }

    fun MobileTokens.toGrpcToken(): MobileToken = mobileToken {
        this.refreshToken = this@toGrpcToken.refreshToken.toGrpc()
        this.accessToken = this@toGrpcToken.accessToken.toGrpc()
    }

    fun IssueNewTokensRequest.toServiceRequest(): RefreshMobileSessionRequest = RefreshMobileSessionRequest(
        token = this@toServiceRequest.refreshToken,
        fp = this@toServiceRequest.deviceFp,
    )

    fun LoginByPhoneVerifyResponse.toGrpcContract() = phoneLoginVerifyResponse {
        this.sessionId = this@toGrpcContract.sessionId.toString()
        this@toGrpcContract.tokens?.let { this.mobileToken = it.toGrpcToken() }
    }

    private fun AccessToken.toGrpc(): TimedToken = timedToken {
        this.token = this@toGrpc.token
        this.expiration = this@toGrpc.expiresAt.toTimeStamp()
    }

    private fun RefreshToken.toGrpc(): TimedToken = timedToken {
        this.token = this@toGrpc.token
        this.expiration = this@toGrpc.expiresAt.toTimeStamp()
    }

    private fun Instant.toTimeStamp() = timestamp {
        this@timestamp.nanos = this@toTimeStamp.nano
        this@timestamp.seconds = this@toTimeStamp.epochSecond
    }
}
