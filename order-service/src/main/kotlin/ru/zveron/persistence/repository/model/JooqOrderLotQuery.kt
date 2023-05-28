package ru.zveron.persistence.repository.model

import org.jooq.*
import ru.zveron.persistence.jooq.models.ORDER_LOT

data class JooqOrderLotQuery(
    val seekParams: List<Any>,
    val filterConditions: List<Condition>,
    val sortParams: List<SortField<Any>>,
    val pageSize: Int = 10,
) {
    companion object {
        private val SELECTION_FIELDS = mutableListOf<SelectField<*>>(
            ORDER_LOT.ID,
            ORDER_LOT.ANIMAL_ID,
            ORDER_LOT.PRICE,
            ORDER_LOT.CREATED_AT,
            ORDER_LOT.SUBWAY_ID,
            ORDER_LOT.TITLE,
            ORDER_LOT.SERVICE_DATE_FROM,
            ORDER_LOT.SERVICE_DATE_TO,
            ORDER_LOT.SERVICE_TYPE,
            ORDER_LOT.SERVICE_DELIVERY_TYPE,
        )

        private val TABLE = ORDER_LOT
    }

    fun toSortedQuery(ctx: DSLContext): SelectForUpdateStep<Record> = ctx.select(SELECTION_FIELDS)
        .from(TABLE)
        .where(filterConditions)
        .orderBy(sortParams)
        .let {
            if (seekParams.isNotEmpty()) it.seek(*seekParams.toTypedArray()).limit(pageSize)
            else it.limit(pageSize)
        }
}
