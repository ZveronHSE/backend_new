package ru.zveron.order.persistence.repository.model

import org.jooq.*
import ru.zveron.order.persistence.jooq.models.ORDER_LOT

data class JooqOrderLotQuery(
        val seekParams: List<Any>,
        val filterConditions: List<Condition>,
        val sortParams: List<SortField<Any>>,
        val selectParams: List<SelectField<*>>,
        val pageSize: Int = 10,
        val table: Table<Record>,
) {
    fun getQuery(ctx: DSLContext) = ctx.select(selectParams)
            .from(ORDER_LOT)
            .where(filterConditions)
            .orderBy(sortParams)
            .let {
                if (seekParams.isNotEmpty()) it.seek(*seekParams.toTypedArray()).limit(pageSize)
                else it.limit(pageSize)
            }
}

class JooqOrderLotQueryBuilder {
    private val seekParams: MutableList<Any> = mutableListOf()
    private val filterConditions: MutableList<Condition> = mutableListOf()
    private val sortParams: MutableList<SortField<Any>> = mutableListOf()
    private val extraSelectFields: MutableSet<Field<*>> = mutableSetOf()
    private var pageSize: Int = 10
    private var lastId: Long? = null

    companion object {
        private val selectionFields = mutableListOf<SelectField<*>>(
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

    fun addSeekParam(seekParam: Any): JooqOrderLotQueryBuilder {
        this.seekParams.add(seekParam)
        return this
    }

    fun filterConditions(filterConditions: List<Condition>): JooqOrderLotQueryBuilder {
        this.filterConditions.addAll(filterConditions)
        return this
    }

    fun addSortParam(sortParam: SortField<Any>): JooqOrderLotQueryBuilder {
        this.sortParams.add(sortParam)
        return this
    }

    fun extraSelectParams(extraSelectParams: List<Field<*>>): JooqOrderLotQueryBuilder {
        this.extraSelectFields.addAll(extraSelectParams)
        return this
    }

    fun pageSize(pageSize: Int): JooqOrderLotQueryBuilder {
        this.pageSize = pageSize
        return this
    }

    fun lastOrderLotId(lastId: Long?): JooqOrderLotQueryBuilder {
        this.lastId = lastId
        return this
    }

    fun build(): JooqOrderLotQuery {
        //default sort for no sort or when records are equal
        @Suppress("UNCHECKED_CAST")
        sortParams.add(ORDER_LOT.ID.desc() as SortField<Any>)

        //todo: map would probably suit it better
        //if last record is present, then add it to seek params as lsat param to match sorting
        lastId?.let {
            seekParams.add(it)
        }

        return JooqOrderLotQuery(
                seekParams = seekParams,
                filterConditions = filterConditions,
                sortParams = sortParams,
                selectParams = selectionFields + extraSelectFields,
                pageSize = pageSize,
                table = TABLE,
        )
    }
}
