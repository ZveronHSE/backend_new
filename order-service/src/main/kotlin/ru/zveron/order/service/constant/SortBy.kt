package ru.zveron.order.service.constant

import org.jooq.Record
import org.jooq.TableField
import ru.zveron.order.persistence.jooq.models.ORDER_LOT

enum class SortBy(val field: TableField<Record, *>) {
    BY_ID(ORDER_LOT.ID),
    BY_DATE_CREATED(ORDER_LOT.CREATED_AT),
    BY_PRICE(ORDER_LOT.PRICE),
    BY_DISTANCE(ORDER_LOT.SUBWAY_ID),
}