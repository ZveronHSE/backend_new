package ru.zveron.service

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import mu.KLogging
import org.springframework.stereotype.Service
import ru.zveron.client.address.SubwayGrpcClient
import ru.zveron.client.address.dto.GetSubwayStationApiResponse
import ru.zveron.client.animal.AnimalGrpcClient
import ru.zveron.client.animal.dto.GetAnimalApiResponse
import ru.zveron.exception.ClientException
import ru.zveron.persistence.model.constant.Status
import ru.zveron.persistence.repository.WaterfallStorage
import ru.zveron.service.constant.Field
import ru.zveron.service.constant.Operation
import ru.zveron.service.mapper.ModelMapper.of
import ru.zveron.service.mapper.ResponseMapper.toGetOrderWaterfallResponse
import ru.zveron.service.model.Animal
import ru.zveron.service.model.FilterParam
import ru.zveron.service.model.GetWaterfallCountRequest
import ru.zveron.service.model.GetWaterfallRequest
import ru.zveron.service.model.SubwayStation
import ru.zveron.service.model.WaterfallOrderLot

@Service
class GetWaterfallService(
    private val waterfallStorage: WaterfallStorage,
    private val subwayGrpcClient: ru.zveron.client.address.SubwayGrpcClient,
    private val animalGrpcClient: AnimalGrpcClient,
) {

    companion object : KLogging()

    suspend fun getWaterfall(request: GetWaterfallRequest): List<WaterfallOrderLot> {
        val orderLotRecords = waterfallStorage.findAllPaginated(
            lastId = request.lastOrderId,
            pageSize = request.pageSize,
            filterParams = request.filterParams + listOf(
                FilterParam(
                    Field.STATUS,
                    Operation.NOT_IN,
                    Status.terminalStatuses().joinToString(","),
                ),
            ),
        )

        val subwayStation =
            coroutineScope { orderLotRecords.map { async { it.subwayId to getSubwayStation(it.subwayId) } } }
        val animals = coroutineScope { orderLotRecords.map { async { it.animalId to getAnimal(it.animalId) } } }

        logger.debug { "Mapping response to service response" }
        return toGetOrderWaterfallResponse(
            orderLotRecords,
            subwayStation.awaitAll()
                .also { logger.debug { "Received all subway station responses" } }
                .filter { it.first != null && it.second != null }
                .associate { it.first!! to it.second!! },
            animals.awaitAll()
                .also { logger.debug { "Received all animals" } }
                .toMap(),
        )
    }

    suspend fun getWaterfallCount(request: GetWaterfallCountRequest): Int {
        return waterfallStorage.countFiltered(
            filterParams = request.filterParams + listOf(
                FilterParam(
                    Field.STATUS,
                    Operation.NOT_IN,
                    Status.terminalStatuses().joinToString(","),
                ),
            ),
        )
    }

    // todo: move to decorator
    private suspend fun getSubwayStation(subwayId: Int?): SubwayStation? {
        if (subwayId == null) return null
        return when (val response = subwayGrpcClient.getSubwayStation(subwayId)) {
            is GetSubwayStationApiResponse.Error -> throw ClientException(
                message = "Get subway station client request failed",
                status = response.error,
            )

            GetSubwayStationApiResponse.NotFound -> null

            is GetSubwayStationApiResponse.Success -> SubwayStation.of(response.subwayStation)
        }
    }

    private suspend fun getAnimal(animalId: Long): Animal? =
        when (val response = animalGrpcClient.getAnimal(animalId)) {
            is GetAnimalApiResponse.Error -> throw ClientException(
                message = "Get animal client request failed",
                status = response.error,
            )

            GetAnimalApiResponse.NotFound -> null

            is GetAnimalApiResponse.Success -> Animal.of(response.animal)
        }
}
