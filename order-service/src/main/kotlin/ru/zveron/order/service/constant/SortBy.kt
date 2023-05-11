package ru.zveron.order.service.constant

import org.jooq.Field
import org.jooq.SortField
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
        val subwayIdField: Field<Int> = ORDER_LOT.SUBWAY_ID,
        val sortedIds: List<Int?> = emptyList(),
    ) : SortBy() {

        fun getSortedIds(sortDirection: SortDirection) =
            if (sortDirection == SortDirection.ASC) sortedIds else sortedIds.reversed()

        fun getSortField(sortDirection: SortDirection): SortField<Int> = subwayIdField
            .sort(getSortedIds(sortDirection).associateWith { getSortedIds(sortDirection).indexOf(it) })
            .let {
                if (sortDirection == SortDirection.ASC) it.nullsFirst() else it.nullsLast()
            }
    }
}
