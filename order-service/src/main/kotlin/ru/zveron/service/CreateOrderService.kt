package ru.zveron.service

import mu.KLogging
import net.logstash.logback.marker.Markers.aggregate
import net.logstash.logback.marker.Markers.append
import org.springframework.stereotype.Service
import ru.zveron.component.ClientDecorator
import ru.zveron.persistence.repository.OrderLotRepository
import ru.zveron.service.mapper.ModelMapper.toOrderLot
import ru.zveron.service.mapper.ResponseMapper
import ru.zveron.service.model.CreateOrderRequest
import ru.zveron.service.model.FullOrderData

@Service
class CreateOrderService(
    private val orderLotRepository: OrderLotRepository,
    private val clientDecorator: ClientDecorator,
) {

    companion object : KLogging()

    suspend fun createOrder(request: CreateOrderRequest): FullOrderData {
        logger.debug(
            aggregate(
                append("profileId", request.profileId),
                append("animalId", request.animalId),
                append("subwayId", request.subwayId)
            )
        ) { "Calling client decorator" }

        val orderExtraData = clientDecorator.getFullOrderData(
            profileId = request.profileId,
            animalId = request.animalId,
            subwayId = request.subwayId
        )

        val orderEntity = orderLotRepository.save(request.toOrderLot())

        return ResponseMapper.mapToFullOrderData(
            o = orderEntity,
            subway = orderExtraData.subwayStation,
            profile = orderExtraData.profile,
            animal = orderExtraData.animal,
        )
    }
}
