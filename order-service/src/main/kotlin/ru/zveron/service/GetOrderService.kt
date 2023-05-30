package ru.zveron.service

import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import mu.KLogging
import net.logstash.logback.marker.Markers.append
import org.springframework.stereotype.Service
import ru.zveron.component.ClientDecorator
import ru.zveron.exception.OrderNotFoundException
import ru.zveron.persistence.repository.OrderLotRepository
import ru.zveron.persistence.repository.StatisticsStorage
import ru.zveron.service.model.ProfileOrder
import ru.zveron.service.mapper.ResponseMapper.mapToFullOrderData
import ru.zveron.service.mapper.ResponseMapper.mapToProfileOrders
import ru.zveron.service.model.FullOrderData

@Service
class GetOrderService(
    private val orderLotRepository: OrderLotRepository,
    private val statisticsStorage: StatisticsStorage,
    private val clientDecorator: ClientDecorator,
) {

    companion object : KLogging()

    suspend fun getOrder(orderId: Long): FullOrderData = supervisorScope {
        val order = orderLotRepository.findById(orderId) ?: throw OrderNotFoundException(orderId)

        logger.debug(append("orderId", order.id)) { "Got order and calling clients to collect data" }

        launch {
            statisticsStorage.incrementViewCount(orderId)
        }

        val orderExtraData = clientDecorator.getFullOrderData(order.profileId, order.animalId, order.subwayId)

        mapToFullOrderData(
            o = order,
            subway = orderExtraData.subwayStation,
            profile = orderExtraData.profile,
            animal = orderExtraData.animal,
        )
    }

    suspend fun getProfileOrders(profileId: Long): List<ProfileOrder> {
        val orders = orderLotRepository.findAllByProfileId(profileId)
        logger.debug(append("profileId", profileId)) { "Got orders and calling clients to collect data" }

        val animalsData = clientDecorator.getAnimalsData(orders.map { it.animalId })
        val orderLotToViewCount =
            statisticsStorage.getOrderLotToViewCount(orders.map { it.id ?: error("Illegal state of order, no id") })

        return mapToProfileOrders(orders, animalsData, orderLotToViewCount)
    }
}
