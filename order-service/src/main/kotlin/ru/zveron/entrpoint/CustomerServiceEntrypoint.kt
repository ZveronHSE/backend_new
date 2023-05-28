package ru.zveron.entrpoint

import com.google.protobuf.Empty
import mu.KLogging
import net.devh.boot.grpc.server.service.GrpcService
import net.logstash.logback.marker.Markers.append
import ru.zveron.contract.order.external.GetCustomerRequest
import ru.zveron.contract.order.external.GetCustomerResponse
import ru.zveron.contract.order.external.GetCustomerResponseKt
import ru.zveron.contract.order.external.GetOrdersByProfileResponse
import ru.zveron.contract.order.external.GetOrdersByProfileResponseKt
import ru.zveron.contract.order.external.OrderCustomerServiceExternalGrpcKt
import ru.zveron.library.grpc.util.GrpcUtils
import ru.zveron.entrpoint.mapper.ResponseMapper.of
import ru.zveron.service.CustomerService
import ru.zveron.service.GetOrderService
import kotlin.coroutines.coroutineContext

@GrpcService
class CustomerServiceEntrypoint(
    private val customerService: CustomerService,
    private val getOrderService: GetOrderService,
) : OrderCustomerServiceExternalGrpcKt.OrderCustomerServiceExternalCoroutineImplBase() {

    companion object : KLogging()

    // todo: should probably move it to the profile service
    override suspend fun getCustomer(request: GetCustomerRequest): GetCustomerResponse {
        require(request.profileId > 0) { "Customer id must be positive" }

        logger.debug(append("request", request)) { "Calling get customer entrypoint" }
        val serviceResponse = customerService.getCustomer(request.profileId)

        logger.debug(append("serviceResponse", serviceResponse)) { "Returning service response" }
        return GetCustomerResponseKt.of(serviceResponse)
    }

    override suspend fun getOrdersByProfile(request: Empty): GetOrdersByProfileResponse {
        val profileId = GrpcUtils.getMetadata(coroutineContext).profileId!!

        logger.debug(append("profileId", profileId)) { "Calling get orders by profile service from entrypoint" }

        return GetOrdersByProfileResponseKt.of(getOrderService.getProfileOrders(profileId))
    }
}
