package ru.zveron.order.client.address

import io.grpc.Status
import io.grpc.StatusException
import io.kotest.matchers.types.shouldBeTypeOf
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import ru.zveron.contract.address.internal.GetSubwayStationRequest
import ru.zveron.contract.address.internal.SubwayStationInternalServiceGrpcKt
import ru.zveron.contract.address.internal.getSubwayStationResponse
import ru.zveron.order.client.address.dto.GetSubwayStationApiResponse
import ru.zveron.order.util.randomId
import ru.zveron.order.util.testSubwayStation

class SubwayGrpcClientTest {

    private val stub = mockk<SubwayStationInternalServiceGrpcKt.SubwayStationInternalServiceCoroutineStub>()

    private val client = SubwayGrpcClient(stub)

    @Test
    fun `given request, when stub returns response, then return success response`() {
        //prep data
        val request = randomId().toInt()

        //prep env
        coEvery { stub.getSubwayStation(any<GetSubwayStationRequest>(), any()) } returns getSubwayStationResponse {
            subwayStation = testSubwayStation()
        }

        //when
        val response = runBlocking {
            client.getSubwayStation(request)
        }

        response.shouldBeTypeOf<GetSubwayStationApiResponse.Success>()
    }

    @Test
    fun `given request, when stub returns not found, then return not found response`() {
        //prep data
        val request = randomId().toInt()

        //prep env
        coEvery {
            stub.getSubwayStation(
                any<GetSubwayStationRequest>(),
                any()
            )
        } throws StatusException(Status.NOT_FOUND)

        //when
        val response = runBlocking {
            client.getSubwayStation(request)
        }

        response.shouldBeTypeOf<GetSubwayStationApiResponse.NotFound>()
    }

    @Test
    fun `given request, when stub returns unknown error, then return error`() {
        //prep data
        val request = randomId().toInt()

        //prep env
        coEvery { stub.getSubwayStation(any<GetSubwayStationRequest>(), any()) } throws StatusException(Status.INTERNAL)

        //when
        val response = runBlocking {
            client.getSubwayStation(request)
        }

        response.shouldBeTypeOf<GetSubwayStationApiResponse.Error>()
    }
}
