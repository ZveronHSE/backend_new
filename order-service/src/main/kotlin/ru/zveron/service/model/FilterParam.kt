package ru.zveron.service.model

import ru.zveron.service.constant.Field
import ru.zveron.service.constant.Operation

data class FilterParam(
    val field: Field,
    val operation: Operation,
    val value: String,
)
