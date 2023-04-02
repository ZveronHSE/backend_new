package ru.zveron.authservice.component.thirdparty.mapper

import ru.zveron.authservice.component.thirdparty.model.ThirdPartyUserInfo
import ru.zveron.authservice.webclient.thirdparty.model.UserInfo

object ThirdPartyMapper {
    fun ThirdPartyUserInfo.Companion.of(userInfo: UserInfo) = ThirdPartyUserInfo(
        firstName = userInfo.firstName ?: "user",
        lastName = userInfo.lastName ?: userInfo.providerUserId,
        userId = userInfo.providerUserId,
        email = userInfo.email,
    )
}
