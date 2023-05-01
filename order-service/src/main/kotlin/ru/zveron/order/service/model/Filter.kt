package ru.zveron.order.service.model

import org.jooq.Record
import org.jooq.TableField
import ru.zveron.order.service.constant.Field
import ru.zveron.order.service.constant.Operation

data class Filter(
    val field: Field,
    val operation: Operation,
    val value: String,
)

@Suppress("UNCHECKED_CAST")
fun Filter.toJooqFilter() = operation.operation(field.field as TableField<Record, Any>, value)