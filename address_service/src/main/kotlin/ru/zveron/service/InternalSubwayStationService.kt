package ru.zveron.service

import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.contract.address.internal.GetSubwayFilteredByDistanceRequest
import ru.zveron.contract.address.internal.GetSubwayFilteredByDistanceResponse
import ru.zveron.contract.address.internal.GetSubwayStationRequest
import ru.zveron.contract.address.internal.GetSubwayStationResponse
import ru.zveron.contract.address.internal.GetSubwayStationsRequest
import ru.zveron.contract.address.internal.GetSubwayStationsResponse
import ru.zveron.contract.address.internal.IntSubwayStationKt
import ru.zveron.contract.address.internal.SubwayStationInternalServiceGrpcKt
import ru.zveron.contract.address.internal.getSubwayStationResponse
import ru.zveron.contract.address.internal.getSubwayStationsResponse
import ru.zveron.exception.SubwayNotFoundException
import ru.zveron.mapper.SubwayStationMapper.ofEntity
import ru.zveron.repository.SubwayStationRepository

@GrpcService
class InternalSubwayStationService(
    private val repository: SubwayStationRepository,
) : SubwayStationInternalServiceGrpcKt.SubwayStationInternalServiceCoroutineImplBase() {

    override suspend fun getSubwayFilteredByDistance(request: GetSubwayFilteredByDistanceRequest): GetSubwayFilteredByDistanceResponse {
        throw NotImplementedError()
    }

    override suspend fun getSubwayStation(request: GetSubwayStationRequest): GetSubwayStationResponse =
        repository.findById(request.id)
            ?.let { IntSubwayStationKt.ofEntity(it) }
            ?.let { getSubwayStationResponse { this.subwayStation = it } }
            ?: throw SubwayNotFoundException(request.id)

    override suspend fun getSubwayStations(request: GetSubwayStationsRequest): GetSubwayStationsResponse =
        repository.findAllByIdIn(request.idsList)
            .map { IntSubwayStationKt.ofEntity(it) }
            .let { getSubwayStationsResponse { subwayStations.addAll(it) } }
}