package ru.zveron.order.service.constant

import com.fasterxml.jackson.annotation.JsonCreator
import org.jooq.Record
import org.jooq.TableField
import ru.zveron.order.persistence.jooq.models.ORDER_LOT

enum class Field(val field: TableField<Record, *>) {
    ID(ORDER_LOT.ID),
    CREATED_AT(ORDER_LOT.CREATED_AT),
    PRICE(ORDER_LOT.PRICE),
    STATION_ID(ORDER_LOT.SUBWAY_ID),
    SERVICE_DELIVERY_TYPE(ORDER_LOT.SERVICE_DELIVERY_TYPE),
    ORDER_SERVICE_TYPE(ORDER_LOT.SERVICE_TYPE),
    ;

    companion object {

        @JvmStatic
        @JsonCreator
        fun ofName(name: String) = values().firstOrNull { it.name.equals(name, true) }
            ?: throw IllegalStateException("Unknown filter field with name=$name")
    }
}