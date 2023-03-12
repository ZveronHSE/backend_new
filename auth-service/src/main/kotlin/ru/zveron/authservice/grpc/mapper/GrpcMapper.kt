package ru.zveron.authservice.grpc.mapper

import com.google.protobuf.timestamp
import ru.zveron.authservice.component.auth.model.RefreshMobileSessionRequest
import ru.zveron.authservice.component.jwt.model.AccessToken
import ru.zveron.authservice.component.jwt.model.MobileTokens
import ru.zveron.authservice.component.jwt.model.RefreshToken
import ru.zveron.authservice.component.thirdparty.contant.ThirdPartyProviderType
import ru.zveron.authservice.grpc.client.model.PhoneNumber
import ru.zveron.authservice.grpc.client.model.RegisterBySocialMediaRequest
import ru.zveron.authservice.service.model.JwtMobileTokens
import ru.zveron.authservice.service.model.LoginByPhoneInitRequest
import ru.zveron.authservice.service.model.LoginByPhoneVerifyRequest
import ru.zveron.authservice.service.model.LoginByPhoneVerifyResponse
import ru.zveron.authservice.service.model.LoginBySocialMediaRequest
import ru.zveron.authservice.util.PhoneNumberParser
import ru.zveron.contract.auth.external.AuthProvider
import ru.zveron.contract.auth.external.IssueNewTokensRequest
import ru.zveron.contract.auth.external.LoginByPasswordRequest
import ru.zveron.contract.auth.external.LoginBySocialRequest
import ru.zveron.contract.auth.external.MobileToken
import ru.zveron.contract.auth.external.PhoneLoginInitRequest
import ru.zveron.contract.auth.external.PhoneLoginVerifyRequest
import ru.zveron.contract.auth.external.PhoneRegisterRequest
import ru.zveron.contract.auth.external.TimedToken
import ru.zveron.contract.auth.external.mobileToken
import ru.zveron.contract.auth.external.phoneLoginVerifyResponse
import ru.zveron.contract.auth.external.timedToken
import ru.zveron.contract.profile.createProfileRequest
import ru.zveron.contract.profile.model.gmail
import ru.zveron.contract.profile.model.links
import ru.zveron.contract.profile.model.phone
import ru.zveron.contract.profile.model.vk
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
        fingerprint = this@toServiceRequest.deviceFp,
    )

    fun LoginByPhoneVerifyResponse.toGrpcContract() = phoneLoginVerifyResponse {
        this@toGrpcContract.sessionId?.let { this.sessionId = it.toString() }
        this@toGrpcContract.tokens?.let { this.mobileToken = it.toGrpcToken() }
    }

    fun PhoneRegisterRequest.toServiceRequest() = ru.zveron.authservice.service.model.RegisterByPhoneRequest(
        fingerprint = this@toServiceRequest.deviceFp,
        sessionId = this@toServiceRequest.sessionId.let { UUID.fromString(it) },
        password = this@toServiceRequest.password.toByteArray(),
        name = this@toServiceRequest.name,
        surname = this@toServiceRequest.surname
    )

    fun ru.zveron.authservice.grpc.client.model.RegisterByPhoneRequest.toClientRequest() = createProfileRequest {
        this.name = this@toClientRequest.name
        this.links = links {
            this.phone = phone {
                this.number = this@toClientRequest.phone.toRequest()
            }
        }
    }

    fun LoginByPasswordRequest.toServiceRequest() = ru.zveron.authservice.service.model.LoginByPasswordRequest(
        loginPhone = PhoneNumberParser.stringToServicePhone(this.phoneNumber),
        password = this.password.toByteArray(),
        fingerprint = this.deviceFp
    )

    fun PhoneNumber.toRequest() = "$countryCode$phone"

    fun LoginBySocialRequest.toServiceRequest() = LoginBySocialMediaRequest(
        accessToken = this.accessToken,
        providerType = this.authProvider.toServiceProvider(),
        providerUserId = this.providerUserId,
        fingerprint = this.deviceFp,
    )

    fun RegisterBySocialMediaRequest.toClientRequest() = createProfileRequest {
        this.name = this@toClientRequest.userInfo.firstName
        this.surname = this@toClientRequest.userInfo.lastName
        this.links = links {
            when (provider) {
                ThirdPartyProviderType.VK -> vk {
                    this.id = this@toClientRequest.userInfo.userId
                    this.ref = "https://vk.com/${this@toClientRequest.userInfo.userId}"
                    this.email = this@toClientRequest.userInfo.email ?: ""
                }

                ThirdPartyProviderType.GMAIL -> gmail {
                    this.id = this@toClientRequest.userInfo.userId
                    this.email = this@toClientRequest.userInfo.email ?: ""
                }
            }
        }
    }

    private fun AuthProvider.toServiceProvider() = when (this) {
        AuthProvider.VK -> ThirdPartyProviderType.VK
        AuthProvider.GMAIL -> ThirdPartyProviderType.GMAIL
        else -> throw IllegalArgumentException()
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
