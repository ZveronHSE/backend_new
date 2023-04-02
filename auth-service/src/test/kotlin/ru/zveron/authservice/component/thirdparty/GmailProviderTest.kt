package ru.zveron.authservice.component.thirdparty

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import ru.zveron.authservice.config.ThirdPartyProviderProperties
import ru.zveron.authservice.exception.SocialMediaException
import ru.zveron.authservice.util.randomAccessToken
import ru.zveron.authservice.util.testUserInfoGoogle
import ru.zveron.authservice.webclient.thirdparty.ThirdPartyClient
import ru.zveron.authservice.webclient.thirdparty.model.GetThirdPartyUserInfoFailure
import ru.zveron.authservice.webclient.thirdparty.model.GetThirdPartyUserInfoSuccess
import ru.zveron.authservice.webclient.thirdparty.model.UserInfoGoogle

class GmailProviderTest {
    private val client = mockk<ThirdPartyClient>()

    private val props = mockk<ThirdPartyProviderProperties>().also {
        coEvery { it.gmail.host } returns "http://localhost:8080"
    }

    private val gmailProvider = GmailProvider(client = client, props)

    @Test
    fun `given access token in request, when client responds with success, then return user info`() {
        //prep data
        val accessToken = randomAccessToken().token

        val userInfo = testUserInfoGoogle()

        //prep env
        coEvery { client.getUserInfo<UserInfoGoogle>(any(), any()) } returns GetThirdPartyUserInfoSuccess(userInfo)

        //when
        val response = runBlocking {
            gmailProvider.getUserInfo(accessToken)
        }

        //then
        response.asClue {
            it.firstName shouldBe userInfo.firstName
            it.lastName shouldBe userInfo.lastName
            it.userId shouldBe userInfo.providerUserId
            it.email shouldBe userInfo.email
        }
    }

    @Test
    fun `given access token in request, when client responds with failure, then throw exception`() {
        //prep data
        val accessToken = randomAccessToken().token

        //prep env
        coEvery {
            client.getUserInfo<UserInfoGoogle>(
                any(),
                any()
            )
        } returns GetThirdPartyUserInfoFailure(HttpStatus.BAD_REQUEST, "something went wrong")

        //when
        runBlocking {
            //then
            shouldThrow<SocialMediaException> {
                gmailProvider.getUserInfo(accessToken)
            }
        }
    }
}
