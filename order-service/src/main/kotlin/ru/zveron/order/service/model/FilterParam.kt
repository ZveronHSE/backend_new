package ru.zveron.order.service.model

import ru.zveron.order.service.constant.Field
import ru.zveron.order.service.constant.Operation

data class FilterParam(
    val field: Field,
    val operation: Operation,
    val value: String,
)