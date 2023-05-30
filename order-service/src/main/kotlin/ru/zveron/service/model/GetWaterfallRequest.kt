package ru.zveron.service.model

data class GetWaterfallRequest(
    val pageSize: Int = 20,
    val lastOrderId: Long? = null,
    val sort: Sort? = null,
    val filterParams: List<FilterParam> = emptyList(),
)
