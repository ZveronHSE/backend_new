package ru.zveron.authservice.service

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import ru.zveron.authservice.component.auth.Authenticator
import ru.zveron.authservice.component.thirdparty.GoogleProvider
import ru.zveron.authservice.component.thirdparty.contant.ThirdPartyProviderType
import ru.zveron.authservice.exception.SocialMediaException
import ru.zveron.authservice.grpc.client.ProfileServiceClient
import ru.zveron.authservice.grpc.client.model.ProfileFound
import ru.zveron.authservice.grpc.client.model.ProfileNotFound
import ru.zveron.authservice.grpc.client.model.RegisterProfileAlreadyExists
import ru.zveron.authservice.grpc.client.model.RegisterProfileSuccess
import ru.zveron.authservice.util.randomId
import ru.zveron.authservice.util.randomName
import ru.zveron.authservice.util.randomSurname
import ru.zveron.authservice.util.randomTokens
import ru.zveron.authservice.util.testLoginBySocialMediaRequest
import ru.zveron.authservice.util.testThirdPartyUserInfo

class LoginBySocialMediaServiceTest {

    private val profileServiceClient = mockk<ProfileServiceClient>()

    private val authenticator = mockk<Authenticator>()

    private val googleProvider = mockk<GoogleProvider>().also {
        coEvery { it.providerType } returns ThirdPartyProviderType.GMAIL
    }

    private val service = LoginBySocialMediaService(
        thirdPartyProviders = listOf(googleProvider),
        profileServiceClient = profileServiceClient,
        authenticator = authenticator,
    )

    @Test
    fun `given login by social request, when userId mmatches and profile doesnt exist, then register new profile`() {
        //prepare data
        val request = testLoginBySocialMediaRequest()


        //prepare env
        coEvery { googleProvider.getUserInfo(any()) } returns testThirdPartyUserInfo().copy(userId = request.providerUserId)

        coEvery { profileServiceClient.findProfileBySocialMedia(any(), any()) } returns ProfileNotFound

        coEvery { profileServiceClient.registerProfileBySocialMedia(any()) } returns RegisterProfileSuccess(randomId())

        coEvery { authenticator.loginUser(any(), any()) } returns randomTokens()

        //when
        runBlocking {
            //then
            shouldNotThrowAny {
                service.loginBySocialMedia(request)
            }
        }
    }

    @Test
    fun `given login by social request, when userId does not match, then throw exception`() {
        //prepare data
        val request = testLoginBySocialMediaRequest()


        //prepare env
        coEvery { googleProvider.getUserInfo(any()) } returns testThirdPartyUserInfo()

        //when
        runBlocking {
            //then
            shouldThrow<SocialMediaException> {
                service.loginBySocialMedia(request)
            }
        }
    }

    @Test
    fun `given login by social request, when userId matches and profile exists, then login`() {
        //prepare data
        val profileId = randomId()
        val profileFoundResponse = ProfileFound(
            id = profileId,
            name = randomName(),
            surname = randomSurname(),
        )
        val request = testLoginBySocialMediaRequest()

        //prepare env
        coEvery { googleProvider.getUserInfo(any()) } returns testThirdPartyUserInfo().copy(userId = request.providerUserId)

        coEvery { profileServiceClient.findProfileBySocialMedia(any(), any()) } returns profileFoundResponse

        coEvery { authenticator.loginUser(any(), any()) } returns randomTokens()

        //when
        runBlocking {
            shouldNotThrowAny {
                service.loginBySocialMedia(request)
            }
        }

        //then
        coVerify(exactly = 0) { profileServiceClient.registerProfileBySocialMedia(any()) }
    }

    @Test
    fun `given login by social request, when userId matches and profile doesnt exist and registration fails bc occupied, then throw exception`() {
        //prepare data
        val request = testLoginBySocialMediaRequest()

        //prepare env
        coEvery { googleProvider.getUserInfo(any()) } returns testThirdPartyUserInfo().copy(userId = request.providerUserId)

        coEvery { profileServiceClient.findProfileBySocialMedia(any(), any()) } returns ProfileNotFound

        coEvery { profileServiceClient.registerProfileBySocialMedia(any()) } returns RegisterProfileAlreadyExists

        //when
        runBlocking {
            //then
            shouldThrow<IllegalStateException> {
                service.loginBySocialMedia(request)
            }
        }
    }
}
