package ru.zveron.client.address

import io.grpc.Status
import io.grpc.StatusException
import mu.KLogging
import net.logstash.logback.marker.Markers.append
import ru.zveron.contract.address.internal.SubwayStationInternalServiceGrpcKt
import ru.zveron.contract.address.internal.getSubwayStationRequest
import ru.zveron.contract.address.internal.getSubwayStationsRequest
import ru.zveron.client.address.dto.GetSubwayStationApiResponse
import ru.zveron.service.mapper.ModelMapper.of
import ru.zveron.service.model.SubwayStation

class SubwayGrpcClient(
    private val subwayGrpcStub: SubwayStationInternalServiceGrpcKt.SubwayStationInternalServiceCoroutineStub,
) {

    companion object : KLogging()

    suspend fun getSubwayStation(subwayStationId: Int): GetSubwayStationApiResponse {
        logger.debug(append("subwayStationId", subwayStationId)) { "Calling get subway station from subway client" }
        val request = getSubwayStationRequest { id = subwayStationId }

        return try {
            val response = subwayGrpcStub.getSubwayStation(request)
            logger.debug { "Received response=$response for $subwayStationId" }
            GetSubwayStationApiResponse.Success(response.subwayStation)
        } catch (e: StatusException) {
            when (e.status.code) {
                Status.Code.NOT_FOUND -> GetSubwayStationApiResponse.NotFound
                else -> GetSubwayStationApiResponse.Error(e.status, e.message)
            }
        }
    }

    suspend fun getSubways(subwayStationIds: List<Int>): ru.zveron.client.address.GetSubwaysApiResponse {
        logger.debug(
            append(
                "subwayStationIds",
                subwayStationIds,
            ),
        ) { "Calling get subway stations batch from subway client" }
        val request = getSubwayStationsRequest { this.ids.addAll(subwayStationIds) }

        return try {
            val response = subwayGrpcStub.getSubwayStations(request)
            ru.zveron.client.address.GetSubwaysApiResponse.Success(response.subwayStationsList.map { SubwayStation.of(it) })
        } catch (e: StatusException) {
            ru.zveron.client.address.GetSubwaysApiResponse.Error(e.status, e.message)
        }
    }
}
