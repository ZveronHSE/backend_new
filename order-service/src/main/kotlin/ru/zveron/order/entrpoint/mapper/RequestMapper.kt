package ru.zveron.order.entrpoint.mapper

import ru.zveron.contract.order.external.CreateOrderRequest
import ru.zveron.contract.order.external.Filter
import ru.zveron.contract.order.external.GetFilteredCountRequest
import ru.zveron.contract.order.external.GetWaterfallRequest
import ru.zveron.contract.order.external.Operation
import ru.zveron.contract.order.external.SortBy
import ru.zveron.contract.order.external.SortDir
import ru.zveron.order.entrpoint.mapper.CommonDtoMapper.toLocalDate
import ru.zveron.order.entrpoint.mapper.CommonDtoMapper.toLocalTime
import ru.zveron.order.service.constant.Field
import ru.zveron.order.service.constant.SortDirection
import ru.zveron.order.service.model.GetWaterfallCountRequest
import ru.zveron.order.service.model.Sort

@Suppress("unused")
object RequestMapper {
    fun GetWaterfallRequest.toServiceRequest() = ru.zveron.order.service.model.GetWaterfallRequest(
        pageSize = pageSize,
        lastOrderId = takeIf { it.hasLastOrderId() }?.let { this.lastOrderId },
        sort = this.sort.toServiceSort(),
        filterParams = this.filtersList.map { it.toServiceFilter() }
    )

    fun GetFilteredCountRequest.toServiceRequest() = GetWaterfallCountRequest(
        filterParams = this.filtersList.map { it.toServiceFilter() }
    )

    fun CreateOrderRequest.toServiceRequest(profileId: Long) = ru.zveron.order.service.model.CreateOrderRequest(
        profileId = profileId,
        animalId = this.animalId,
        subwayId = takeIf { this.hasSubwayStationId() }?.let { this.subwayStationId },
        description = this.description,
        price = this.price,
        title = this.title,
        serviceDateFrom = this.serviceDateFrom.toLocalDate(),
        serviceDateTo = this.serviceDateTo.toLocalDate(),
        timeWindowFrom = this.serviceTimeFrom.toLocalTime(),
        timeWindowTo = this.serviceTimeTo.toLocalTime(),
        serviceType = ru.zveron.order.persistence.model.constant.ServiceType.byAlias(this.serviceType.name),
        serviceDeliveryType = ru.zveron.order.persistence.model.constant.ServiceDeliveryType.byAlias(this.deliveryMethod.name),
    )

    private fun ru.zveron.contract.order.external.Sort.toServiceSort() = this.sortBy.toServiceSortBy()?.let {
        Sort(
            sortBy = it,
            sortDirection = this.sortDir.toServiceDirection()
        )
    }

    private fun SortBy.toServiceSortBy() = when (this) {
        SortBy.BY_DISTANCE -> ru.zveron.order.service.constant.SortBy.ByDistance()
        SortBy.BY_PRICE -> ru.zveron.order.service.constant.SortBy.ByPrice()
        SortBy.BY_SERVICE_DELIVERY -> ru.zveron.order.service.constant.SortBy.ByServiceDate()
        SortBy.DEFAULT -> null
        else -> error("Wrong sort by type")
    }

    private fun SortDir.toServiceDirection() = SortDirection.valueOf(this.name)

    private fun Filter.toServiceFilter() = ru.zveron.order.service.model.FilterParam(
        field = Field.ofName(this.field.name),
        operation = this.operation.toServiceOperation(),
        value = this.value,
    )

    private fun Operation.toServiceOperation() = ru.zveron.order.service.constant.Operation.valueOf(this.name)
}
