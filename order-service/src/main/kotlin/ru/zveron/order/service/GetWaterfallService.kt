package ru.zveron.order.service

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import ru.zveron.order.client.address.SubwayGrpcClient
import ru.zveron.order.client.address.dto.GetSubwayStationApiResponse
import ru.zveron.order.client.animal.AnimalGrpcClient
import ru.zveron.order.client.animal.dto.GetAnimalApiResponse
import ru.zveron.order.exception.ClientException
import ru.zveron.order.persistence.repository.WaterfallStorage
import ru.zveron.order.service.model.Animal
import ru.zveron.order.service.model.GetWaterfallRequest
import ru.zveron.order.service.model.SubwayStation
import ru.zveron.order.service.model.WaterfallOrderLot
import ru.zveron.order.service.mapper.ModelMapper.of
import ru.zveron.order.service.mapper.ResponseMapper.toGetOrderWaterfallResponse


@Service
class GetWaterfallService(
    private val waterfallStorage: WaterfallStorage,
    private val subwayGrpcClient: SubwayGrpcClient,
    private val animalGrpcClient: AnimalGrpcClient,
) {

    suspend fun getWaterfall(request: GetWaterfallRequest): List<WaterfallOrderLot> {
        val orderLotRecords = waterfallStorage.findAllPaginated(
            lastId = request.lastOrderId,
            pageSize = request.pageSize,
        )

        val subwayStation =
            coroutineScope { orderLotRecords.map { async { it.subwayId to getSubwayStation(it.subwayId) } } }
        val animals = coroutineScope { orderLotRecords.map { async { it.animalId to getAnimal(it.animalId) } } }

        return toGetOrderWaterfallResponse(
            orderLotRecords,
            subwayStation.awaitAll().toMap(),
            animals.awaitAll().toMap()
        )
    }

    private suspend fun getSubwayStation(subwayId: Int): SubwayStation? =
        when (val response = subwayGrpcClient.getSubwayStation(subwayId)) {
            is GetSubwayStationApiResponse.Error -> throw ClientException(
                message = "Get subway station client request failed",
                status = response.error
            )

            GetSubwayStationApiResponse.NotFound -> null

            is GetSubwayStationApiResponse.Success -> SubwayStation.of(response.subwayStation)
        }

    private suspend fun getAnimal(animalId: Long): Animal? =
        when (val response = animalGrpcClient.getAnimal(animalId)) {
            is GetAnimalApiResponse.Error -> throw ClientException(
                message = "Get animal client request failed",
                status = response.error
            )

            GetAnimalApiResponse.NotFound -> null

            is GetAnimalApiResponse.Success -> Animal.of(response.animal)
        }
}
