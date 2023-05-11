package ru.zveron.order.entrpoint.mapper

import ru.zveron.contract.order.external.Filter
import ru.zveron.contract.order.external.GetWaterfallRequest
import ru.zveron.contract.order.external.Operation
import ru.zveron.contract.order.external.SortBy
import ru.zveron.contract.order.external.SortDir
import ru.zveron.order.service.constant.Field
import ru.zveron.order.service.constant.SortDirection
import ru.zveron.order.service.model.Sort

object RequestMapper {
    fun GetWaterfallRequest.toServiceRequest() = ru.zveron.order.service.model.GetWaterfallRequest(
        pageSize = pageSize,
        lastOrderId = takeIf { it.hasLastOrderId() }?.let { this.lastOrderId },
        sort = this.sort.toServiceSort(),
        filterParams = this.filtersList.map { it.toServiceFilter() }
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
        SortBy.BY_DATE_CREATED -> ru.zveron.order.service.constant.SortBy.ByServiceDate()
        SortBy.BY_ID -> null //todo: replace with default for default values
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