package ru.zveron.authservice.component.thirdparty.mapper

import ru.zveron.authservice.component.thirdparty.model.ThirdPartyUserInfo
import ru.zveron.authservice.webclient.thirdparty.model.UserInfoGoogle

object ThirdPartyMapper {
    fun ThirdPartyUserInfo.Factory.of(userInfo: UserInfoGoogle) = ThirdPartyUserInfo(
        firstName = userInfo.name,
        lastName = userInfo.family_name,
        userId = userInfo.sub,
        email = userInfo.email,
    )
}
