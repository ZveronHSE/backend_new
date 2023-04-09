package ru.zveron.authservice.component.thirdparty

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.springframework.http.HttpStatus
import ru.zveron.authservice.config.ThirdPartyProviderProperties
import ru.zveron.authservice.exception.SocialMediaException
import ru.zveron.authservice.util.randomAccessToken
import ru.zveron.authservice.util.testUserInfoMailru
import ru.zveron.authservice.webclient.thirdparty.ThirdPartyClient
import ru.zveron.authservice.webclient.thirdparty.model.GetThirdPartyUserInfoFailure
import ru.zveron.authservice.webclient.thirdparty.model.GetThirdPartyUserInfoSuccess
import ru.zveron.authservice.webclient.thirdparty.model.UserInfoMailru

class MailruProviderTest {
    private val client = mockk<ThirdPartyClient>()

    private val props = mockk<ThirdPartyProviderProperties>().also {
        coEvery { it.mailru.host } returns "http://localhost:8080"
    }

    private val provider = MailruProvider(client = client, props)

    @Test
    fun `given correct access token in request, when client responds successfully, then return user info`() {
        //prep data
        val accessToken = randomAccessToken().token
        val userInfo = testUserInfoMailru()

        //prep env
        coEvery { client.getUserInfo<UserInfoMailru>(any(), any()) } returns GetThirdPartyUserInfoSuccess(userInfo)

        //when
        val response = runBlocking {
            shouldNotThrowAny {
                provider.getUserInfo(accessToken)
            }
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
            client.getUserInfo<UserInfoMailru>(
                any(),
                any()
            )
        } returns GetThirdPartyUserInfoFailure(HttpStatus.BAD_REQUEST, "something went wrong")

        //when
        runBlocking {
            //then
            shouldThrow<SocialMediaException> {
                provider.getUserInfo(accessToken)
            }
        }
    }
}
