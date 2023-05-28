package ru.zveron.order.entrpoint

import mu.KLogging
import net.devh.boot.grpc.server.service.GrpcService
import net.logstash.logback.marker.Markers.append
import ru.zveron.contract.order.external.CreateOrderRequest
import ru.zveron.contract.order.external.CreateOrderResponse
import ru.zveron.contract.order.external.CreateOrderResponseKt
import ru.zveron.contract.order.external.GetOrderRequest
import ru.zveron.contract.order.external.GetOrderResponse
import ru.zveron.contract.order.external.GetOrderResponseKt
import ru.zveron.contract.order.external.OrderServiceExternalGrpcKt
import ru.zveron.library.grpc.util.GrpcUtils
import ru.zveron.order.entrpoint.mapper.RequestMapper.toServiceRequest
import ru.zveron.order.entrpoint.mapper.ResponseMapper.of
import ru.zveron.order.entrpoint.validator.ServiceRequestValidator
import ru.zveron.order.service.CreateOrderService
import ru.zveron.order.service.GetOrderService
import kotlin.coroutines.coroutineContext

@GrpcService
class OrderServiceEntrypoint(
    private val getOrderService: GetOrderService,
    private val createOrderService: CreateOrderService,
) : OrderServiceExternalGrpcKt.OrderServiceExternalCoroutineImplBase() {

    companion object : KLogging()

    override suspend fun getOrder(request: GetOrderRequest): GetOrderResponse {
        require(request.id > 0) { "Order id must be positive" }

        logger.debug(append("id", request.id)) { "Calling get order service from entrypoint" }
        return GetOrderResponseKt.of(getOrderService.getOrder(request.id))
    }

    override suspend fun createOrder(request: CreateOrderRequest): CreateOrderResponse {
        val profileId =
            GrpcUtils.getMetadata(coroutineContext = coroutineContext, requiredAuthorized = true).profileId!!

        ServiceRequestValidator.validate(request)

        val serviceResponse = createOrderService.createOrder(request.toServiceRequest(profileId))

        return CreateOrderResponseKt.of(serviceResponse)
    }
}
