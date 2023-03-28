package ru.zveron.authservice.grpc

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.authservice.component.jwt.JwtDecoder
import ru.zveron.authservice.config.BaseAuthTest
import ru.zveron.authservice.grpc.client.model.ProfileFound
import ru.zveron.authservice.grpc.client.model.ProfileNotFound
import ru.zveron.authservice.grpc.client.model.RegisterProfileSuccess
import ru.zveron.authservice.util.randomId
import ru.zveron.authservice.util.testLoginBySocialGrpcRequest
import ru.zveron.authservice.util.testUserInfoGoogle
import ru.zveron.authservice.webclient.ThirdPartyStubs

class LoginBySocialMediaControllerTest : BaseAuthTest() {

    @Autowired
    lateinit var controller: AuthExternalController

    @Autowired
    lateinit var jwtDecoder: JwtDecoder

    @Test
    fun `happy pass registration`() {
        //prep data
        val profileId = randomId()
        val providerUserId = randomId()
        val googleUserInfo = testUserInfoGoogle().copy(
            sub = providerUserId.toString()
        )
        val request = testLoginBySocialGrpcRequest()

        //prep env
        coEvery { profileClient.findProfileBySocialMedia(any(), any()) } returns ProfileNotFound
        coEvery { profileClient.registerProfileBySocialMedia(any()) } returns RegisterProfileSuccess(profileId)

        ThirdPartyStubs.serverStubForGoogleGetUserInfo(googleUserInfo)

        //when
        val response = runBlocking {
            controller.loginBySocial(request)
        }

        //then
        val decodedAccessToken = jwtDecoder.decodeAccessToken(response.accessToken.token)
        decodedAccessToken.profileId shouldBe profileId

        val decodedRefreshToken = jwtDecoder.decodeRefreshToken(response.refreshToken.token)
        decodedRefreshToken.profileId shouldBe profileId
    }

    @Test
    fun `happy pass login`() {
        //prep data
        val profileId = randomId()
        val providerUserId = randomId()
        val googleUserInfo = testUserInfoGoogle().copy(
            sub = providerUserId.toString()
        )
        val profile = ProfileFound(id = profileId, name = googleUserInfo.name, surname = googleUserInfo.family_name)
        val request = testLoginBySocialGrpcRequest()

        //prep env
        coEvery { profileClient.findProfileBySocialMedia(any(), any()) } returns profile

        ThirdPartyStubs.serverStubForGoogleGetUserInfo(googleUserInfo)

        //when
        val response = runBlocking {
            controller.loginBySocial(request)
        }

        //then
        val decodedAccessToken = jwtDecoder.decodeAccessToken(response.accessToken.token)
        decodedAccessToken.profileId shouldBe profileId

        val decodedRefreshToken = jwtDecoder.decodeRefreshToken(response.refreshToken.token)
        decodedRefreshToken.profileId shouldBe profileId
    }
}
