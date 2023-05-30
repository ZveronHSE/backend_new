package ru.zveron.entrpoint

import mu.KLogging
import net.devh.boot.grpc.server.service.GrpcService
import net.logstash.logback.marker.Markers.append
import ru.zveron.contract.order.external.GetFilteredCountRequest
import ru.zveron.contract.order.external.GetFilteredCountResponse
import ru.zveron.contract.order.external.GetWaterfallRequest
import ru.zveron.contract.order.external.GetWaterfallResponse
import ru.zveron.contract.order.external.GetWaterfallResponseKt
import ru.zveron.contract.order.external.OrderWaterfallServiceExternalGrpcKt
import ru.zveron.contract.order.external.getFilteredCountResponse
import ru.zveron.entrpoint.mapper.RequestMapper.toServiceRequest
import ru.zveron.entrpoint.mapper.ResponseMapper.of
import ru.zveron.entrpoint.validator.ServiceRequestValidator.validate
import ru.zveron.service.GetWaterfallService

@GrpcService
class GetOrderWaterfallEntrypoint(
    private val service: GetWaterfallService,
) : OrderWaterfallServiceExternalGrpcKt.OrderWaterfallServiceExternalCoroutineImplBase() {

    companion object : KLogging()

    override suspend fun getWaterfall(request: GetWaterfallRequest): GetWaterfallResponse {
        validate(request)

        val serviceRequest = request.toServiceRequest()
        logger.debug(
            append(
                "serviceRequest",
                serviceRequest
            )
        ) { "Making get waterfall request in order waterfall entrypoint" }

        val waterfallOrders = service.getWaterfall(serviceRequest)

        logger.debug(append("response", waterfallOrders)) { "Returning response from the service" }
        return GetWaterfallResponseKt.of(waterfallOrders)
    }

    override suspend fun getFilteredCount(request: GetFilteredCountRequest): GetFilteredCountResponse {
        return getFilteredCountResponse { count = service.getWaterfallCount(request.toServiceRequest()) }
    }
}

