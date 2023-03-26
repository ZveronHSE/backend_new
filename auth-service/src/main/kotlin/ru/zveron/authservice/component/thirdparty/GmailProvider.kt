package ru.zveron.authservice.component.thirdparty

import io.grpc.internal.GrpcUtil
import mu.KLogging
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import ru.zveron.authservice.component.thirdparty.contant.ThirdPartyProviderType
import ru.zveron.authservice.component.thirdparty.mapper.ThirdPartyMapper.of
import ru.zveron.authservice.component.thirdparty.model.ThirdPartyUserInfo
import ru.zveron.authservice.config.ThirdPartyProviderProperties
import ru.zveron.authservice.exception.SocialMediaException
import ru.zveron.authservice.webclient.thirdparty.ThirdPartyClient
import ru.zveron.authservice.webclient.thirdparty.model.GetThirdPartyUserInfoFailure
import ru.zveron.authservice.webclient.thirdparty.model.GetThirdPartyUserInfoSuccess
import ru.zveron.authservice.webclient.thirdparty.model.UserInfoGoogle

@Component
@EnableConfigurationProperties(ThirdPartyProviderProperties::class)
class GmailProvider(
    private val client: ThirdPartyClient,
    private val providerProperties: ThirdPartyProviderProperties,
) : ThirdPartyProvider {

    companion object : KLogging() {
        const val USERS_GET_PATH = "/oauth2/v3/userinfo"
    }

    override val providerType: ThirdPartyProviderType = ThirdPartyProviderType.GMAIL

    override suspend fun getUserInfo(accessToken: String): ThirdPartyUserInfo {
        val requestUri = buildGetUserInfoUri(accessToken)
        val clientResponse = client.getUserInfo(requestUri, UserInfoGoogle::class.java)

        return when (clientResponse) {
            is GetThirdPartyUserInfoSuccess -> ThirdPartyUserInfo.of(clientResponse.response)

            is GetThirdPartyUserInfoFailure -> throw SocialMediaException(
                clientResponse.errorMessage,
                GrpcUtil.httpStatusToGrpcStatus(
                    clientResponse.code?.value() ?: HttpStatus.INTERNAL_SERVER_ERROR.value()
                ).code
            )
        }
    }

    private fun buildGetUserInfoUri(accessToken: String) =
        UriComponentsBuilder.fromHttpUrl(providerProperties.gmail.host + USERS_GET_PATH)
            .queryParam("access_token", accessToken)
            .build()
            .toUri()
            .also { ThirdPartyClient.logger.info { "Uri completed as $it" } }
}
