package ru.zveron.order.persistence.repository

import kotlinx.coroutines.reactor.awaitSingle
import mu.KLogging
import net.logstash.logback.marker.Markers.append
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.SelectField
import org.jooq.SortField
import org.jooq.impl.DSL
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import ru.zveron.order.persistence.jooq.models.ORDER_LOT
import ru.zveron.order.persistence.repository.model.OrderLotWrapper
import ru.zveron.order.service.constant.SortBy
import ru.zveron.order.service.constant.SortDirection
import ru.zveron.order.service.model.Filter
import ru.zveron.order.service.model.Sort
import ru.zveron.order.service.model.toJooqFilter


@Component
class WaterfallStorage(
    private val ctx: DSLContext,
    private val repository: OrderLotRepository,
) {

    companion object : KLogging() {
        private val selectionFields = mutableSetOf<SelectField<*>>(
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
    }

    suspend fun findAllPaginated(
        lastId: Long?,
        pageSize: Int,
        filters: List<Filter> = emptyList(),
        sort: Sort? = null,
    ): List<OrderLotWrapper> {

        val lastEntity = lastId?.let {
            repository.findById(it)
        }

        val seek = mutableSetOf<Any>()

        val filterConditions = filters.map { it.toJooqFilter() }

        val sortConditions: MutableList<SortField<Any>> = mutableListOf()

        //todo: separate in a query builder function
        val extraQueryParams = mutableSetOf<Field<*>>()

        when (val sortByCasted = sort?.sortBy) {
            is SortBy.ByDistance -> {
                logger.debug { "Sorting by distance for ids ${sortByCasted.sortedIds}" }
//                extraQueryParams.add(
//                    customOrdering(
//                        ORDER_LOT.SUBWAY_ID,
//                        sortByCasted.sortedIds,
//                    ).`as`("subway_id_ordering")
//                )

                val sortedSubwayIds =
                    sortByCasted.sortedIds.let { if (sort.sortDirection == SortDirection.DESC) it.reversed() else it }
                val sf: SortField<Int> =
                    ORDER_LOT.SUBWAY_ID.sort(sortByCasted.sortedIds.associateWith {
                        sortedSubwayIds.indexOf(it)
                    }
                    )
                        .let {
                            if (sort.sortDirection == SortDirection.ASC) it.nullsFirst() else it.nullsLast()
                        }
                sortConditions.add(sf as SortField<Any>)
                lastId?.run {
                    seek.add(sortedSubwayIds.indexOf(lastEntity?.subwayId) as Any)
                }
            }

            is SortBy.ByPrice -> {
                sortConditions.add(sort.sortDirection.dir(sortByCasted.priceField as Field<Any>))
                lastId?.run {
                    seek.add(lastEntity?.price as Any)
                }
            }

            is SortBy.ByServiceDate -> {
                sortConditions.add(sort.sortDirection.dir(sortByCasted.dateFromField as Field<Any>))
                sortConditions.add(sort.sortDirection.dir(sortByCasted.dateToField as Field<Any>))
            }

            else -> error("Wrong sort by type")
        }

        sortConditions.add(ORDER_LOT.ID.desc() as SortField<Any>)

        lastId?.let { seek.add(it) }

        val query = ctx.select(selectionFields + extraQueryParams)
            .from(ORDER_LOT)
            .where(*filterConditions.toTypedArray())
            .orderBy(sortConditions)
            .let {
                if (lastId != null) it.seek(*seek.toTypedArray()).limit(pageSize)
                else it.limit(pageSize)
            }

        logger.debug(append("query", query.toString())) { "Composed jooq query ${query.toString()}" }

        val result = Flux.from(query)
            .log()
            .map {
                it.into(OrderLotWrapper::class.java)
            }
            .collectList()
            .awaitSingle() ?: emptyList()

        logger.debug(append("result", result.map { it.toString() })) { "Completed request" }

        return result
    }

    private fun customOrdering(field: Field<Int?>, ids: List<Int>): Field<Int?> {
        logger.debug { "Custom ordering for ids $ids" }
        val zeroOnNullField = DSL.coalesce(field, 0)
        val caseExpr = ids.foldIndexed(DSL.`when`(zeroOnNullField.eq(ids[0]), 0)) { index, acc, id ->
            acc.`when`(zeroOnNullField.eq(id), index).also { logger.debug { acc } }
        }.otherwise(ids.size)

        logger.debug { "Custom ordering completed with $caseExpr" }

        return caseExpr
    }
}


