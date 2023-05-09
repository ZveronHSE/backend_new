package ru.zveron.controller.external

import com.google.protobuf.Int64Value
import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.contract.specialist.CardSpecialist
import ru.zveron.contract.specialist.GetWaterfallRequest
import ru.zveron.contract.specialist.GetWaterfallResponse
import ru.zveron.contract.specialist.Sort
import ru.zveron.contract.specialist.SpecialistServiceExternalGrpcKt
import ru.zveron.expection.SpecialistIllegalArgumentException
import ru.zveron.mapper.SpecialistMapper.toWaterfallResponse
import ru.zveron.service.SpecialistService

@GrpcService
class SpecialistController(
    private val specialistService: SpecialistService
) : SpecialistServiceExternalGrpcKt.SpecialistServiceExternalCoroutineImplBase() {
    override suspend fun getWaterfall(request: GetWaterfallRequest): GetWaterfallResponse {
        // Если нет запросов по сортировке, то пагинация и выдача не может работать корректно
        if (request.sort.sortBy == Sort.SortBy.UNRECOGNIZED) {
            throw SpecialistIllegalArgumentException("sort", Sort.SortBy.UNRECOGNIZED)
        }

        if (request.pageSize < 1) {
            throw SpecialistIllegalArgumentException("pageSize", request.pageSize)
        }

        val specialists = specialistService.getWaterfall(request.sort, request.pageSize)

        // TODO reviews / rating
        // TODO addresses

        return specialists.toWaterfallResponse()
    }

    override suspend fun getCardSpecialist(request: Int64Value): CardSpecialist {
        return super.getCardSpecialist(request)
    }
}
