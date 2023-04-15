package ru.zveron.grpc

import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.contract.address.internal.GetSubwayFilteredByDistanceRequest
import ru.zveron.contract.address.internal.GetSubwayFilteredByDistanceResponse
import ru.zveron.contract.address.internal.GetSubwayStationRequest
import ru.zveron.contract.address.internal.GetSubwayStationResponse
import ru.zveron.contract.address.internal.GetSubwayStationsRequest
import ru.zveron.contract.address.internal.GetSubwayStationsResponse
import ru.zveron.contract.address.internal.SubwayStationInternalServiceGrpcKt
import ru.zveron.service.GetSubwayStationService

@GrpcService
class InternalSubwayStationEntrypoint(
    private val service: GetSubwayStationService,
) : SubwayStationInternalServiceGrpcKt.SubwayStationInternalServiceCoroutineImplBase() {

    override suspend fun getSubwayFilteredByDistance(request: GetSubwayFilteredByDistanceRequest): GetSubwayFilteredByDistanceResponse {
        throw NotImplementedError()
    }

    override suspend fun getSubwayStation(request: GetSubwayStationRequest): GetSubwayStationResponse =
        service.getSubwayStation(request)


    override suspend fun getSubwayStations(request: GetSubwayStationsRequest): GetSubwayStationsResponse =
        service.getSubwayStationsByIds(request)

}
