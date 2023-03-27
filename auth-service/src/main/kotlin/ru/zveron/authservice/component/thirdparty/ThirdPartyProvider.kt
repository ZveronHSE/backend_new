package ru.zveron.authservice.component.thirdparty

import ru.zveron.authservice.component.thirdparty.contant.ThirdPartyProviderType
import ru.zveron.authservice.component.thirdparty.model.ThirdPartyUserInfo

interface ThirdPartyProvider {
    val providerType: ThirdPartyProviderType

    suspend fun getUserInfo(accessToken: String): ThirdPartyUserInfo
}