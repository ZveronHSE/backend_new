package ru.zv.authservice.grpc

import ru.zv.authservice.service.dto.JwtMobileTokens
import ru.zv.authservice.service.dto.LoginByPhoneInitRequest
import ru.zv.authservice.service.dto.LoginByPhoneVerifyRequest
import ru.zv.authservice.util.PhoneNumberParser
import ru.zveron.contract.auth.MobileToken
import ru.zveron.contract.auth.PhoneLoginInitRequest
import ru.zveron.contract.auth.PhoneLoginVerifyRequest
import ru.zveron.contract.auth.mobileToken
import java.util.UUID


fun PhoneLoginInitRequest.toServiceRequest() =
    LoginByPhoneInitRequest(phoneNumber = PhoneNumberParser.stringToServicePhone(this.phoneNumber), this.deviceFp)

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