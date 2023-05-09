package ru.zveron.order.service.model

import ru.zveron.order.service.constant.SortBy
import ru.zveron.order.service.constant.SortDirection

data class Sort(
    val sortBy: SortBy,
    val sortDirection: SortDirection,
)