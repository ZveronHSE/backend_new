package ru.zveron.authservice.util

import ru.zveron.authservice.component.thirdparty.contant.ThirdPartyProviderType
import ru.zveron.authservice.component.thirdparty.model.ThirdPartyUserInfo
import ru.zveron.authservice.service.model.LoginBySocialMediaRequest
import ru.zveron.authservice.webclient.thirdparty.model.UserInfoGoogle
import ru.zveron.authservice.webclient.thirdparty.model.UserInfoMailru
import ru.zveron.contract.auth.external.AuthProvider
import ru.zveron.contract.auth.external.loginBySocialRequest
import java.util.UUID

fun testLoginBySocialMediaRequest() = LoginBySocialMediaRequest(
    accessToken = randomAccessToken().token,
    //todo: change when other providers are introduced
    providerType = ThirdPartyProviderType.GMAIL,
    fingerprint = randomDeviceFp(),
)

fun testThirdPartyUserInfo() = ThirdPartyUserInfo(
    randomName(),
    randomSurname(),
    randomId().toString(),
    randomEmail(),
)

fun testUserInfoGoogle() = UserInfoGoogle(
    email = randomEmail(),
    email_verified = false,
    lastName = randomSurname(),
    given_name = randomName(),
    locale = "ru",
    firstName = randomName(),
    picture = "${UUID.randomUUID()}.png",
    providerUserId = randomId().toString(),
)

fun testUserInfoMailru() = UserInfoMailru(
    email = randomEmail(),
    firstName = randomName(),
    lastName = randomSurname(),
    providerUserId = randomId().toString(),
    picture = "${UUID.randomUUID()}.png",
    nickname = randomName(),
    name = randomName(),
    locale = "ru",
    gender = "MALE",
    birthday = randomBirthday().toString(),
)

fun testLoginBySocialGrpcRequest() = loginBySocialRequest {
    this.deviceFp = randomDeviceFp()
    this.accessToken = randomAccessToken().token
    //todo: other providers
    this.authProvider = AuthProvider.GMAIL
}