package ru.zveron.order.entrpoint

import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.contract.order.external.GetOrderRequest
import ru.zveron.contract.order.external.GetOrderResponse
import ru.zveron.contract.order.external.GetOrderResponseKt
import ru.zveron.contract.order.external.OrderServiceExternalGrpcKt
import ru.zveron.order.mapper.entrypoint.of
import ru.zveron.order.service.GetOrderService

@GrpcService
class OrderServiceEntrypoint(
    private val getOrderService: GetOrderService,
) : OrderServiceExternalGrpcKt.OrderServiceExternalCoroutineImplBase() {

    override suspend fun getOrder(request: GetOrderRequest): GetOrderResponse {
        require(request.id > 0) { "Order id must be positive" }

        return GetOrderResponseKt.of(getOrderService.getOrder(request.id))
    }
}
