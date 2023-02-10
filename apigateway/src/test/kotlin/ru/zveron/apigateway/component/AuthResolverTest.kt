package ru.zveron.apigateway.component

import io.grpc.Metadata
import io.grpc.Status
import io.grpc.StatusException
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.zveron.apigateway.component.model.ResolveForRoleRequest
import ru.zveron.apigateway.component.constant.ServiceScope
import ru.zveron.apigateway.exception.ApiGatewayException
import ru.zveron.apigateway.grpc.client.AccessTokenNotValid
import ru.zveron.apigateway.grpc.client.AccessTokenValid
import ru.zveron.apigateway.grpc.client.GrpcAuthClient
import ru.zveron.apigateway.util.randomProfileId
import java.util.UUID

class AuthResolverTest {

    private val authClient = mockk<GrpcAuthClient>()
    private val authResolver = AuthResolver(authClient)

    @Test
    fun `when scope is ANY, then return null`() = runBlocking {
        val request = ResolveForRoleRequest(ServiceScope.ANY, "")

        val serviceResponse = authResolver.resolveForScope(request)
        serviceResponse shouldBe null

        coVerify(exactly = 0) { authClient.verifyAccessToken(any()) }
    }

    @Test
    fun `when scope is BUYER and client returns that token is valid, then return profile id`() = runBlocking {
        val profileId = randomProfileId()
        val token = UUID.randomUUID().toString()
        val request = ResolveForRoleRequest(ServiceScope.BUYER, token)

        coEvery { authClient.verifyAccessToken(any()) } returns AccessTokenValid(profileId)

        val serviceResponse = authResolver.resolveForScope(request)
        serviceResponse shouldBe profileId
    }

    @Test
    fun `when scope is BUYER and client returns that token is not valid, then throw exception`(): Unit = runBlocking {
        val token = UUID.randomUUID().toString()
        val request = ResolveForRoleRequest(ServiceScope.BUYER, token)

        coEvery { authClient.verifyAccessToken(any()) } returns AccessTokenNotValid(
            "error message",
            Status.Code.INTERNAL,
            Metadata()
        )

        assertThrows<ApiGatewayException> {
            authResolver.resolveForScope(request)
        }
    }

    @Test
    fun `when scope is BUYER and token is empty, then throw DATA_LOSS status`(): Unit = runBlocking {
        val token = ""
        val request = ResolveForRoleRequest(ServiceScope.BUYER, token)

        assertThrows<StatusException> {
            authResolver.resolveForScope(request)
        }.status.shouldBe(Status.DATA_LOSS)
    }
}
