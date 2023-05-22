package ru.zveron.order.entrpoint

import mu.KLogging
import net.devh.boot.grpc.server.service.GrpcService
import net.logstash.logback.marker.Markers.append
import ru.zveron.contract.order.external.GetCustomerRequest
import ru.zveron.contract.order.external.GetCustomerResponse
import ru.zveron.contract.order.external.GetCustomerResponseKt
import ru.zveron.contract.order.external.OrderCustomerServiceExternalGrpcKt
import ru.zveron.order.entrpoint.mapper.ResponseMapper.of
import ru.zveron.order.service.CustomerService

@GrpcService
class CustomerServiceEntrypoint(
    private val customerService: CustomerService,
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
}
