package ru.zveron.service

import org.springframework.stereotype.Service
import ru.zveron.contract.address.external.ExtSubwayStationKt
import ru.zveron.contract.address.external.GetSubwayStationsByCityExtRequest
import ru.zveron.contract.address.external.getSubwayStationsExtResponse
import ru.zveron.contract.address.internal.GetSubwayStationRequest
import ru.zveron.contract.address.internal.GetSubwayStationResponse
import ru.zveron.contract.address.internal.GetSubwayStationsRequest
import ru.zveron.contract.address.internal.IntSubwayStationKt
import ru.zveron.contract.address.internal.getSubwayStationResponse
import ru.zveron.contract.address.internal.getSubwayStationsResponse
import ru.zveron.exception.SubwayNotFoundException
import ru.zveron.mapper.SubwayStationMapper.ofEntity
import ru.zveron.repository.SubwayStationRepository

@Service
class GetSubwayStationService(
    private val repository: SubwayStationRepository,
) {

    suspend fun getSubwayStation(request: GetSubwayStationRequest): GetSubwayStationResponse =
        repository.findById(request.id)
            ?.let { IntSubwayStationKt.ofEntity(it) }
            ?.let { getSubwayStationResponse { this.subwayStation = it } }
            ?: throw SubwayNotFoundException(request.id)

    suspend fun getSubwayStationsByIds(request: GetSubwayStationsRequest) = repository.findAllByIdIn(request.idsList)
        .map { IntSubwayStationKt.ofEntity(it) }
        .let { getSubwayStationsResponse { subwayStations.addAll(it) } }

    suspend fun getSubwayStationsRequest(request: GetSubwayStationsByCityExtRequest) =
        repository.findAllByCity(request.city)
            .let {
                getSubwayStationsExtResponse {
                    this.subwayStations.addAll(it.map { entity -> ExtSubwayStationKt.ofEntity(entity) })
                }
            }

}