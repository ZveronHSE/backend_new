package ru.zveron.order.client.address

import io.grpc.Status
import io.grpc.StatusException
import mu.KLogging
import net.logstash.logback.marker.Markers.append
import ru.zveron.contract.address.internal.SubwayStationInternalServiceGrpcKt
import ru.zveron.contract.address.internal.getSubwayStationRequest
import ru.zveron.contract.address.internal.getSubwayStationsRequest
import ru.zveron.order.client.address.dto.GetSubwayStationApiResponse
import ru.zveron.order.service.mapper.ModelMapper.of
import ru.zveron.order.service.model.SubwayStation

class SubwayGrpcClient(
        private val subwayGrpcStub: SubwayStationInternalServiceGrpcKt.SubwayStationInternalServiceCoroutineStub,
) {

    companion object : KLogging()

    suspend fun getSubwayStation(subwayStationId: Int): GetSubwayStationApiResponse {
        logger.debug(append("subwayStationId", subwayStationId)) { "Calling get subway station from subway client" }
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

    suspend fun getSubways(subwayStationIds: List<Int>): GetSubwaysApiResponse {
        logger.debug(append("subwayStationIds", subwayStationIds)) { "Calling get subway stations batch from subway client" }
        val request = getSubwayStationsRequest { this.ids.addAll(subwayStationIds) }

        return try {
            val response = subwayGrpcStub.getSubwayStations(request)
            GetSubwaysApiResponse.Success(response.subwayStationsList.map { SubwayStation.of(it) })
        } catch (e: StatusException) {
            GetSubwaysApiResponse.Error(e.status, e.message)
        }
    }
}
