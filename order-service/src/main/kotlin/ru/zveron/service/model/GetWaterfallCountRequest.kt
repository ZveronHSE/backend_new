package ru.zveron.service.model

data class GetWaterfallCountRequest(
    val filterParams: List<FilterParam> = emptyList(),
)
