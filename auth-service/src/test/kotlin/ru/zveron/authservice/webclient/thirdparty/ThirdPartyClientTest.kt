package ru.zveron.authservice.webclient.thirdparty

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.assertions.asClue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.util.UriComponentsBuilder
import ru.zveron.authservice.component.thirdparty.GmailProvider
import ru.zveron.authservice.util.getHost
import ru.zveron.authservice.util.randomAccessToken
import ru.zveron.authservice.util.testUserInfoGoogle
import ru.zveron.authservice.webclient.BaseWiremockTest
import ru.zveron.authservice.webclient.ThirdPartyStubs
import ru.zveron.authservice.webclient.thirdparty.model.GetThirdPartyUserInfoFailure
import ru.zveron.authservice.webclient.thirdparty.model.GetThirdPartyUserInfoSuccess
import ru.zveron.authservice.webclient.thirdparty.model.UserInfoGoogle

class ThirdPartyClientTest : BaseWiremockTest() {
    private val objectMapper = ObjectMapper().registerKotlinModule()

    private val client = ThirdPartyClient(webClient, objectMapper)

    @Test
    fun `given get user info request for google, when client succeeds, then returns user info`() {
        //prepare data
        val accessToken = randomAccessToken().token
        val host = server.getHost()
        val uri = UriComponentsBuilder.fromHttpUrl(host + GmailProvider.USERS_GET_PATH)
            .queryParam("access_token", accessToken)
            .build()
            .toUri()

        val userInfo = testUserInfoGoogle()

        //prepare env
        ThirdPartyStubs.serverStubForGoogleGetUserInfo(userInfo)

        //when
        val response = runBlocking {
            client.getUserInfo(uri, UserInfoGoogle::class.java)
        }

        //then
        response.shouldNotBeNull()
        val responseAsGoogle = response as GetThirdPartyUserInfoSuccess<UserInfoGoogle>
        responseAsGoogle.response.asClue {
            it shouldBe userInfo
        }
    }

    @Test
    fun `given get user info request for google, when responds 5xx, then returns failure`() {
        //prepare data
        val accessToken = randomAccessToken().token
        val host = server.getHost()
        val uri = UriComponentsBuilder.fromHttpUrl(host + GmailProvider.USERS_GET_PATH)
            .queryParam("access_token", accessToken)
            .build()
            .toUri()

        //prepare env
        ThirdPartyStubs.serverStubForGoogleGetUserInfoFail(HttpStatus.INTERNAL_SERVER_ERROR)

        //when
        val response = runBlocking {
            client.getUserInfo(uri, UserInfoGoogle::class.java)
        }

        //then
        response.shouldNotBeNull()
        val responseAsGoogle = response as GetThirdPartyUserInfoFailure<UserInfoGoogle>
        responseAsGoogle.code shouldBe HttpStatus.INTERNAL_SERVER_ERROR
    }

    @Test
    fun `given get user info request for google, when responds 4xx, then returns failure`() {
        //prepare data
        val accessToken = randomAccessToken().token
        val host = server.getHost()
        val uri = UriComponentsBuilder.fromHttpUrl(host + GmailProvider.USERS_GET_PATH)
            .queryParam("access_token", accessToken)
            .build()
            .toUri()

        //prepare env
        ThirdPartyStubs.serverStubForGoogleGetUserInfoFail(HttpStatus.BAD_REQUEST)

        //when
        val response = runBlocking {
            client.getUserInfo(uri, UserInfoGoogle::class.java)
        }

        //then
        response.shouldNotBeNull()
        val responseAsGoogle = response as GetThirdPartyUserInfoFailure<UserInfoGoogle>
        responseAsGoogle.code shouldBe HttpStatus.BAD_REQUEST
    }
}
