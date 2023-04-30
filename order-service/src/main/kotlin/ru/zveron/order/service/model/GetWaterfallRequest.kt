package ru.zveron.order.service.model

import ru.zveron.order.service.constant.SortBy
import ru.zveron.order.service.constant.SortDirection

data class GetWaterfallRequest(
    val pageSize: Int = 20,
    val lastOrderId: Long? = null,
    val sort: Sort = Sort(sortBy = SortBy.BY_ID, sortDirection = SortDirection.DESC),
    val filters: List<Filter> = emptyList(),
)
