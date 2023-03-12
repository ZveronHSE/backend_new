package ru.zveron.authservice.grpc.client.model

import ru.zveron.authservice.component.thirdparty.contant.ThirdPartyProviderType
import ru.zveron.authservice.component.thirdparty.model.ThirdPartyUserInfo

data class RegisterBySocialMediaRequest(
    val userInfo: ThirdPartyUserInfo,
    val provider: ThirdPartyProviderType,
)