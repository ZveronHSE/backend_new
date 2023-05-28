package ru.zveron.service.model

import ru.zveron.service.constant.SortBy
import ru.zveron.service.constant.SortDirection

data class Sort(
    val sortBy: SortBy,
    val sortDirection: SortDirection,
)