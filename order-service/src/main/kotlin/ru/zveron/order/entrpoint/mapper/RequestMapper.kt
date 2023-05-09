package ru.zveron.order.entrpoint.mapper

import ru.zveron.contract.order.external.*
import ru.zveron.order.service.constant.Field
import ru.zveron.order.service.constant.SortDirection
import ru.zveron.order.service.model.Sort

object RequestMapper {
    fun GetWaterfallRequest.toServiceRequest() = ru.zveron.order.service.model.GetWaterfallRequest(
        pageSize = pageSize,
        lastOrderId = takeIf { it.hasLastOrderId() }?.let { this.lastOrderId },
        sort = this.sort.let {
            Sort(
                sortBy = it.sortBy.toServiceSortBy(),
                sortDirection = it.sortDir.toServiceDirection()
            )
        },
        filters = this.filtersList.map { it.toServiceFilter() }
    )

    private fun SortBy.toServiceSortBy() = ru.zveron.order.service.constant.SortBy.valueOf(this.name)

    private fun SortDir.toServiceDirection() = SortDirection.valueOf(this.name)

    private fun Filter.toServiceFilter() = ru.zveron.order.service.model.Filter(
        field = Field.ofName(this.field.name),
        operation = this.operation.toServiceOperation(),
        value = this.value,
    )

    private fun Operation.toServiceOperation() = ru.zveron.order.service.constant.Operation.valueOf(this.name)
}