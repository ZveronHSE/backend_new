package ru.zveron.order.service

import kotlinx.coroutines.supervisorScope
import mu.KLogging
import net.logstash.logback.marker.Markers.append
import org.springframework.stereotype.Service
import ru.zveron.order.component.ClientDecorator
import ru.zveron.order.exception.OrderNotFoundException
import ru.zveron.order.persistence.repository.OrderLotRepository
import ru.zveron.order.service.mapper.ResponseMapper.mapToFullOrderData
import ru.zveron.order.service.model.FullOrderData

@Service
class GetOrderService(
    private val orderLotRepository: OrderLotRepository,
    private val clientDecorator: ClientDecorator,
) {

    companion object : KLogging()

    suspend fun getOrder(orderId: Long): FullOrderData = supervisorScope {
        val order = orderLotRepository.findById(orderId) ?: throw OrderNotFoundException(orderId)

        logger.debug(append("orderId", order.id)) { "Got order and calling clients to collect data" }

        val orderExtraData = clientDecorator.getFullOrderData(order.profileId, order.animalId, order.subwayId)

        mapToFullOrderData(
            o = order,
            subway = orderExtraData.subwayStation,
            profile = orderExtraData.profile,
            animal = orderExtraData.animal,
        )
    }
}
