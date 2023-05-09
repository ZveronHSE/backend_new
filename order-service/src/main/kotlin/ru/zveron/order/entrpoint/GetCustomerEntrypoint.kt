package ru.zveron.order.entrpoint

import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.contract.order.external.GetCustomerRequest
import ru.zveron.contract.order.external.GetCustomerResponse
import ru.zveron.contract.order.external.GetCustomerResponseKt
import ru.zveron.contract.order.external.OrderCustomerServiceExternalGrpcKt
import ru.zveron.order.entrpoint.mapper.ResponseMapper.of
import ru.zveron.order.service.CustomerService

@GrpcService
class GetCustomerEntrypoint(
    private val customerService: CustomerService,
) : OrderCustomerServiceExternalGrpcKt.OrderCustomerServiceExternalCoroutineImplBase() {

    //todo: should probably move it to the profile service
    override suspend fun getCustomer(request: GetCustomerRequest): GetCustomerResponse {
        require(request.profileId > 0) { "Customer id must be positive" }

        val serviceResponse = customerService.getCustomer(request.profileId)

        return GetCustomerResponseKt.of(serviceResponse)
    }
}
