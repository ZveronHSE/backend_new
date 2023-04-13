package ru.zveron.service

import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.contract.address.external.ExtSubwayStationKt
import ru.zveron.contract.address.external.GetSubwayStationsByCityExtRequest
import ru.zveron.contract.address.external.GetSubwayStationsExtResponse
import ru.zveron.contract.address.external.SubwayStationExternalServiceGrpcKt
import ru.zveron.contract.address.external.getSubwayStationsExtResponse
import ru.zveron.mapper.SubwayStationMapper.ofEntity
import ru.zveron.repository.SubwayStationRepository

@GrpcService
class ExternalSubwayStationService(
    private val repository: SubwayStationRepository,
) : SubwayStationExternalServiceGrpcKt.SubwayStationExternalServiceCoroutineImplBase() {

    override suspend fun getSubwayStationsByCity(request: GetSubwayStationsByCityExtRequest): GetSubwayStationsExtResponse {
        val subwayStations = repository.findAllByCity(request.city)
        return getSubwayStationsExtResponse {
            this.subwayStations.addAll(subwayStations.map { ExtSubwayStationKt.ofEntity(it) })
        }
    }
}
