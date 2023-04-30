package ru.zveron.order.client.profile

import io.grpc.Status
import io.grpc.StatusException
import io.kotest.matchers.types.shouldBeTypeOf
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import ru.zveron.contract.profile.ProfileServiceInternalGrpcKt
import ru.zveron.order.client.profile.dto.GetProfileApiResponse
import ru.zveron.order.test.util.randomId
import ru.zveron.order.test.util.testFindProfileResponse

class ProfileGrpcClientTest {

    private val stub = mockk<ProfileServiceInternalGrpcKt.ProfileServiceInternalCoroutineStub>()

    private val client = ProfileGrpcClient(stub)

    @Test
    fun `given request, when stub returns response, then return success response`() {
        //prep data
        val profileResponse = testFindProfileResponse()

        //prep env
        coEvery { stub.getProfile(any(), any()) } returns profileResponse

        //when
        val response = runBlocking {
            client.getProfile(randomId())
        }

        //then
        response.shouldBeTypeOf<GetProfileApiResponse.Success>()
    }

    @Test
    fun `given request, when stub returns not found exception, then return not found response`() {
        //prep env
        coEvery { stub.getProfile(any(), any()) } throws StatusException(Status.NOT_FOUND)

        //when
        val response = runBlocking {
            client.getProfile(randomId())
        }

        //then
        response.shouldBeTypeOf<GetProfileApiResponse.NotFound>()
    }

    @Test
    fun `given request, when stub returns unknown exception, then return error response`() {
        //prep env
        coEvery { stub.getProfile(any(), any()) } throws StatusException(Status.UNKNOWN)

        //when
        val response = runBlocking {
            client.getProfile(randomId())
        }

        //then
        response.shouldBeTypeOf<GetProfileApiResponse.Error>()
    }
}
