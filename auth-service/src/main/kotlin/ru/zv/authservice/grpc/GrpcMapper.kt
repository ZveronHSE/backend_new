package ru.zv.authservice.grpc

import com.google.protobuf.timestamp
import ru.zv.authservice.service.dto.JwtMobileTokens
import ru.zv.authservice.service.dto.LoginByPhoneInitRequest
import ru.zv.authservice.service.dto.LoginByPhoneVerifyRequest
import ru.zv.authservice.util.PhoneNumberParser
import ru.zveron.contract.auth.MobileToken
import ru.zveron.contract.auth.PhoneLoginInitRequest
import ru.zveron.contract.auth.PhoneLoginVerifyRequest
import ru.zveron.contract.auth.mobileToken
import ru.zveron.contract.auth.timedToken
import java.util.UUID


fun PhoneLoginInitRequest.toServiceRequest() =
    LoginByPhoneInitRequest(phoneNumber = PhoneNumberParser.stringToServicePhone(this.phoneNumber), this.deviceFp)

fun PhoneLoginVerifyRequest.toServiceRequest() =
    LoginByPhoneVerifyRequest(code = this.code, sessionId = UUID.fromString(this.sessionId), deviceFp = this.deviceFp)

fun JwtMobileTokens.toGrpcToken(): MobileToken {
    val refreshToken = this.refreshToken
    val refreshExpiration = this.refreshExpiration
    val accessToken = this.accessToken
    val accessExpiration = this.accessExpiration
    return mobileToken {
        this.refreshToken = timedToken {
            this.token = refreshToken
            this.expiration = timestamp {
                this.seconds = refreshExpiration.epochSecond
                this.nanos = refreshExpiration.nano
            }
        }
        this.accessToken = timedToken {
            this.token = accessToken
            this.expiration = timestamp {
                this.seconds = accessExpiration.epochSecond
                this.nanos = accessExpiration.nano
            }
        }
    }
}