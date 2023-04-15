package ru.zveron.authservice.service

import org.springframework.stereotype.Service
import ru.zveron.authservice.component.auth.Authenticator
import ru.zveron.authservice.component.jwt.model.MobileTokens
import ru.zveron.authservice.component.thirdparty.ThirdPartyProvider
import ru.zveron.authservice.component.thirdparty.contant.ThirdPartyProviderType
import ru.zveron.authservice.component.thirdparty.model.ThirdPartyUserInfo
import ru.zveron.authservice.exception.AuthException
import ru.zveron.authservice.exception.RegistrationException
import ru.zveron.authservice.exception.SocialMediaException
import ru.zveron.authservice.grpc.client.ProfileServiceClient
import ru.zveron.authservice.grpc.client.model.FindProfileUnknownFailure
import ru.zveron.authservice.grpc.client.model.ProfileFound
import ru.zveron.authservice.grpc.client.model.ProfileNotFound
import ru.zveron.authservice.grpc.client.model.RegisterBySocialMediaRequest
import ru.zveron.authservice.grpc.client.model.RegisterProfileAlreadyExists
import ru.zveron.authservice.grpc.client.model.RegisterProfileFailure
import ru.zveron.authservice.grpc.client.model.RegisterProfileSuccess
import ru.zveron.authservice.service.model.LoginBySocialMediaRequest

@Service
class LoginBySocialMediaService(
    thirdPartyProviders: List<ThirdPartyProvider>,
    private val profileServiceClient: ProfileServiceClient,
    private val authenticator: Authenticator,
) {

    private val providerMap = thirdPartyProviders.associateBy { it.providerType }

    suspend fun loginBySocialMedia(request: LoginBySocialMediaRequest): MobileTokens {
        val provider = getProvider(request.providerType)
        val userInfo = provider.getUserInfo(request.accessToken)

        val response = profileServiceClient.findProfileBySocialMedia(request.providerType, userInfo.userId)

        val profileId: Long = when (response) {
            is ProfileFound -> response.id

            ProfileNotFound -> registerNewProfile(request.providerType, userInfo)

            is FindProfileUnknownFailure -> throw AuthException(response.message, response.code, response.metadata)
        }

        return authenticator.loginUser(request.fingerprint, profileId)
    }

    private suspend fun registerNewProfile(providerType: ThirdPartyProviderType, userInfo: ThirdPartyUserInfo): Long {
        val response = profileServiceClient.registerProfileBySocialMedia(
            RegisterBySocialMediaRequest(
                userInfo = userInfo,
                provider = providerType,
            )
        )

        return when (response) {
            is RegisterProfileSuccess -> response.profileId
            //if account already exists, then we simply find its id and log in
            is RegisterProfileAlreadyExists -> error("Profile not found by social media, but already exists for $userInfo")

            is RegisterProfileFailure -> throw RegistrationException(
                response.message,
                response.code.code,
                response.metadata
            )
        }
    }

    private fun getProvider(providerType: ThirdPartyProviderType) =
        providerMap[providerType] ?: throw SocialMediaException("Unknown social media provider type $providerType")
}
