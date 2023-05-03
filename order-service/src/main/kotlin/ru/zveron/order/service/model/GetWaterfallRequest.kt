package ru.zveron.order.service.model

data class GetWaterfallRequest(
    val pageSize: Int = 20,
    val lastOrderId: Long? = null,
    val sort: Sort? = null,
    val filters: List<Filter> = emptyList(),
)
