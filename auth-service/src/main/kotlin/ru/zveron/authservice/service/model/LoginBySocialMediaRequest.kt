package ru.zveron.authservice.service.model

import ru.zveron.authservice.component.thirdparty.contant.ThirdPartyProviderType

data class LoginBySocialMediaRequest(
    val accessToken: String,
    val providerType: ThirdPartyProviderType,
    val providerUserId: String,
    val fingerprint: String,
)
