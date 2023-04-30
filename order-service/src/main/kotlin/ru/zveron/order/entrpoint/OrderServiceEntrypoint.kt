package ru.zveron.order.entrpoint

import mu.KLogging
import net.devh.boot.grpc.server.service.GrpcService
import net.logstash.logback.marker.Markers.append
import ru.zveron.contract.order.external.GetOrderRequest
import ru.zveron.contract.order.external.GetOrderResponse
import ru.zveron.contract.order.external.GetOrderResponseKt
import ru.zveron.contract.order.external.OrderServiceExternalGrpcKt
import ru.zveron.order.entrpoint.mapper.ResponseMapper.of
import ru.zveron.order.service.GetOrderService

@GrpcService
class OrderServiceEntrypoint(
    private val getOrderService: GetOrderService,
) : OrderServiceExternalGrpcKt.OrderServiceExternalCoroutineImplBase() {

    companion object : KLogging()

    override suspend fun getOrder(request: GetOrderRequest): GetOrderResponse {
        require(request.id > 0) { "Order id must be positive" }

        logger.debug(append("id", request.id)) { "Calling get order service from entrypoint" }
        return GetOrderResponseKt.of(getOrderService.getOrder(request.id))
    }
}
