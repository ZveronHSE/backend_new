package ru.zveron.order.entrpoint

import mu.KLogging
import net.devh.boot.grpc.server.service.GrpcService
import net.logstash.logback.marker.Markers.append
import ru.zveron.contract.order.external.GetWaterfallRequest
import ru.zveron.contract.order.external.GetWaterfallResponse
import ru.zveron.contract.order.external.GetWaterfallResponseKt
import ru.zveron.contract.order.external.OrderWaterfallServiceExternalGrpcKt
import ru.zveron.order.entrpoint.mapper.RequestMapper.toServiceRequest
import ru.zveron.order.entrpoint.mapper.ResponseMapper.of
import ru.zveron.order.entrpoint.validator.ServiceRequestValidator.validate
import ru.zveron.order.service.GetWaterfallService

@GrpcService
class OrderWaterfallServiceEntrypoint(
    private val service: GetWaterfallService
) : OrderWaterfallServiceExternalGrpcKt.OrderWaterfallServiceExternalCoroutineImplBase() {

    companion object : KLogging()

    override suspend fun getWaterfall(request: GetWaterfallRequest): GetWaterfallResponse {
        validate(request)

        val serviceRequest = request.toServiceRequest()
        logger.debug(
            append(
                "serviceRequest",
                serviceRequest)) { "Making get waterfall request in order waterfall entrypoint" }

        val waterfallOrders = service.getWaterfall(serviceRequest)

        logger.debug(append("response", waterfallOrders)) { "Returning response from the service" }
        return GetWaterfallResponseKt.of(waterfallOrders)
    }
}
