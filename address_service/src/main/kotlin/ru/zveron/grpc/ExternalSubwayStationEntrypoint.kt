package ru.zveron.grpc

import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.contract.address.external.GetSubwayStationsByCityExtRequest
import ru.zveron.contract.address.external.SubwayStationExternalServiceGrpcKt
import ru.zveron.service.GetSubwayStationService

@GrpcService
class ExternalSubwayStationEntrypoint(
    private val service: GetSubwayStationService,
) : SubwayStationExternalServiceGrpcKt.SubwayStationExternalServiceCoroutineImplBase() {

    override suspend fun getSubwayStationsByCity(request: GetSubwayStationsByCityExtRequest) =
        service.getSubwayStationsRequest(request)
}
