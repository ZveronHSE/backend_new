package ru.zveron.order.client.address

import io.grpc.Status
import io.grpc.StatusException
import ru.zveron.contract.address.internal.SubwayStationInternalServiceGrpcKt
import ru.zveron.contract.address.internal.getSubwayStationRequest
import ru.zveron.order.client.address.dto.GetSubwayStationApiResponse

class SubwayGrpcClient(
    private val subwayGrpcStub: SubwayStationInternalServiceGrpcKt.SubwayStationInternalServiceCoroutineStub,
) {

    suspend fun getSubwayStation(subwayStationId: Int): GetSubwayStationApiResponse {
        val request = getSubwayStationRequest { id = subwayStationId }

        return try {
            val response = subwayGrpcStub.getSubwayStation(request)
            GetSubwayStationApiResponse.Success(response.subwayStation)
        } catch (e: StatusException) {
            when (e.status.code) {
                Status.Code.NOT_FOUND -> GetSubwayStationApiResponse.NotFound
                else -> GetSubwayStationApiResponse.Error(e.status, e.message)
            }
        }
    }
}

