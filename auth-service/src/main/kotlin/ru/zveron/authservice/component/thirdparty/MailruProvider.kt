package ru.zveron.authservice.component.thirdparty

import mu.KLogging
import net.logstash.logback.marker.Markers.append
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
import ru.zveron.authservice.webclient.thirdparty.model.UserInfoMailru

@Component
class MailruProvider(
    private val client: ThirdPartyClient,
    properties: ThirdPartyProviderProperties,
) : ThirdPartyProvider {

    companion object : KLogging() {
        const val GET_USER_INFO_PATH = "/userinfo"
    }

    override val providerType = ThirdPartyProviderType.MAILRU

    private val props = properties.mailru
    override suspend fun getUserInfo(accessToken: String): ThirdPartyUserInfo {
        val uri = buildGetUserInfoUrl(props.host + GET_USER_INFO_PATH, accessToken)
            .also { logger.debug(append("uri", it)) { "Get user info request" } }

        return when (val response = client.getUserInfo(uri, UserInfoMailru::class.java)) {
            is GetThirdPartyUserInfoFailure -> throw SocialMediaException.of(response)

            is GetThirdPartyUserInfoSuccess -> ThirdPartyUserInfo.of(response.response)
        }
    }
}
