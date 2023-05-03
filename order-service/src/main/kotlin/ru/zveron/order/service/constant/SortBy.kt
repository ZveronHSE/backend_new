package ru.zveron.order.service.constant

import org.jooq.Field
import org.jooq.impl.DSL
import ru.zveron.order.persistence.jooq.models.ORDER_LOT
import java.sql.Date

sealed class SortBy {

    data class ByServiceDate(
            val dateFromField: Field<Date> = ORDER_LOT.SERVICE_DATE_FROM,
            val dateToField: Field<Date> = ORDER_LOT.SERVICE_DATE_TO,
    ) : SortBy()

    data class ByPrice(
            val priceField: Field<Long> = ORDER_LOT.PRICE,
    ) : SortBy()

    data class ByDistance(
            val subwayIdOrderingField: Field<Any> = DSL.field("subway_id_ordering"),
            val sortedIds: List<Int?> = emptyList(),
    ) : SortBy()
}
