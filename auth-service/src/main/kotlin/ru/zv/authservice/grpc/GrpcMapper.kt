package ru.zv.authservice.grpc

import com.google.protobuf.timestamp
import ru.zv.authservice.component.auth.RefreshMobileSessionRequest
import ru.zv.authservice.component.jwt.AccessToken
import ru.zv.authservice.component.jwt.MobileTokens
import ru.zv.authservice.component.jwt.RefreshToken
import ru.zv.authservice.service.dto.JwtMobileTokens
import ru.zv.authservice.service.dto.LoginByPhoneInitRequest
import ru.zv.authservice.service.dto.LoginByPhoneVerifyRequest
import ru.zv.authservice.util.PhoneNumberParser
import ru.zveron.contract.auth.IssueNewTokensRequest
import ru.zveron.contract.auth.MobileToken
import ru.zveron.contract.auth.PhoneLoginInitRequest
import ru.zveron.contract.auth.PhoneLoginVerifyRequest
import ru.zveron.contract.auth.TimedToken
import ru.zveron.contract.auth.mobileToken
import ru.zveron.contract.auth.timedToken
import java.util.UUID

fun PhoneLoginInitRequest.toServiceRequest() =
    LoginByPhoneInitRequest(phoneNumber = PhoneNumberParser.stringToServicePhone(this.phoneNumber), this.deviceFp)

fun PhoneLoginVerifyRequest.toServiceRequest() =
    LoginByPhoneVerifyRequest(code = this.code, sessionId = UUID.fromString(this.sessionId), deviceFp = this.deviceFp)

fun JwtMobileTokens.toGrpcToken(): MobileToken = mobileToken {
    this.accessToken = timedToken {
        this.token = this@toGrpcToken.accessToken
        this.expiration = toTimeStamp()
    }
    this.refreshToken = timedToken {
        this.token = this@toGrpcToken.refreshToken
        this.expiration = toTimeStamp()
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

fun AccessToken.toGrpc(): TimedToken = timedToken {
    this.token = this@toGrpc.token
    this.expiration = toTimeStamp()
}

fun RefreshToken.toGrpc(): TimedToken = timedToken {
    this.token = this@toGrpc.token
    this.expiration = toTimeStamp()
}

fun toTimeStamp() = timestamp {
    this@timestamp.nanos = this.nanos
    this@timestamp.seconds = this.seconds
}
