package ru.zveron.order.service.model

data class GetWaterfallCountRequest(
    val filterParams: List<FilterParam> = emptyList(),
)
