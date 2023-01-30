package ru.zveron.authservice.grpc

import com.google.protobuf.timestamp
import ru.zveron.authservice.service.model.JwtMobileTokens
import ru.zveron.authservice.service.model.LoginByPhoneInitRequest
import ru.zveron.authservice.service.model.LoginByPhoneVerifyRequest
import ru.zveron.authservice.util.PhoneNumberParser
import ru.zveron.contract.auth.MobileToken
import ru.zveron.contract.auth.PhoneLoginInitRequest
import ru.zveron.contract.auth.PhoneLoginVerifyRequest
import ru.zveron.contract.auth.mobileToken
import ru.zveron.contract.auth.timedToken
import java.util.UUID

object GrpcMapper {
    fun PhoneLoginInitRequest.toServiceRequest() =
        LoginByPhoneInitRequest(phoneNumber = PhoneNumberParser.stringToServicePhone(this.phoneNumber), this.deviceFp)

    fun PhoneLoginVerifyRequest.toServiceRequest() =
        LoginByPhoneVerifyRequest(
            code = this.code,
            sessionId = UUID.fromString(this.sessionId),
            deviceFingerprint = this.deviceFp
        )

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
}
