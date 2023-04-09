package ru.zveron.authservice.component.thirdparty

import mu.KLogging
import net.logstash.logback.marker.Markers.append
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import ru.zveron.authservice.component.thirdparty.contant.ThirdPartyProviderType
import ru.zveron.authservice.component.thirdparty.mapper.ThirdPartyMapper.of
import ru.zveron.authservice.component.thirdparty.model.ThirdPartyUserInfo
import ru.zveron.authservice.config.ThirdPartyProviderProperties
import ru.zveron.authservice.exception.SocialMediaException
import ru.zveron.authservice.util.ThirdPartyUtils.buildGetUserInfoUrl
import ru.zveron.authservice.webclient.thirdparty.ThirdPartyClient
import ru.zveron.authservice.webclient.thirdparty.model.GetThirdPartyUserInfoFailure
import ru.zveron.authservice.webclient.thirdparty.model.GetThirdPartyUserInfoSuccess
import ru.zveron.authservice.webclient.thirdparty.model.UserInfoGoogle

@Component
@EnableConfigurationProperties(ThirdPartyProviderProperties::class)
class GmailProvider(
    private val client: ThirdPartyClient,
    providerProperties: ThirdPartyProviderProperties,
) : ThirdPartyProvider {

    companion object : KLogging() {
        const val GET_USER_INFO_PATH = "/oauth2/v3/userinfo"
    }

    private val props = providerProperties.gmail
    override val providerType: ThirdPartyProviderType = ThirdPartyProviderType.GMAIL

    override suspend fun getUserInfo(accessToken: String): ThirdPartyUserInfo {
        val uri = buildGetUserInfoUrl(props.host + GET_USER_INFO_PATH, accessToken)
            .also { logger.debug(append("uri", it)) { "Get user info request" } }

        return when (val response = client.getUserInfo(uri, UserInfoGoogle::class.java)) {
            is GetThirdPartyUserInfoFailure -> throw SocialMediaException.of(response)

            is GetThirdPartyUserInfoSuccess -> ThirdPartyUserInfo.of(response.response)
        }
    }
}
