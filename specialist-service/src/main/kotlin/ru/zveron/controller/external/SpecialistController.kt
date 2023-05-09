package ru.zveron.controller.external

import com.google.protobuf.Int64Value
import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.contract.specialist.CardSpecialist
import ru.zveron.contract.specialist.GetWaterfallRequest
import ru.zveron.contract.specialist.GetWaterfallResponse
import ru.zveron.contract.specialist.SpecialistServiceExternalGrpcKt
import ru.zveron.service.SpecialistService

@GrpcService
class SpecialistController(
    private val specialistService: SpecialistService
) : SpecialistServiceExternalGrpcKt.SpecialistServiceExternalCoroutineImplBase() {
    override suspend fun getWaterfall(request: GetWaterfallRequest): GetWaterfallResponse {
        return super.getWaterfall(request)
    }

    override suspend fun getCardSpecialist(request: Int64Value): CardSpecialist {
        return super.getCardSpecialist(request)
    }
}