package ru.zveron.order.persistence.repository

import mu.KLogging
import net.logstash.logback.marker.Markers.append
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.TableField
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import ru.zveron.order.persistence.jooq.models.ORDER_LOT
import ru.zveron.order.persistence.repository.model.OrderLotWrapper
import ru.zveron.order.service.constant.SortBy
import ru.zveron.order.service.constant.SortDirection
import ru.zveron.order.service.model.Filter
import ru.zveron.order.service.model.Sort


@Component
class WaterfallStorage(
    private val ctx: DSLContext,
) {

    companion object : KLogging()

    suspend fun findAllPaginated(
        lastId: Long?,
        pageSize: Int,
        filters: List<Filter> = emptyList(),
        sort: Sort = Sort(SortBy.BY_ID, SortDirection.DESC)
    ): List<OrderLotWrapper> {

        @Suppress("UNCHECKED_CAST")
        val filterConditions =
            filters.map { it.operation.operation(it.field.field as TableField<Record, Any>, it.value) }

        @Suppress("UNCHECKED_CAST")
        val sortConditions =
            mutableSetOf(sort.sortDirection.sortOrder(sort.sortBy.field as TableField<Record, Any>))

        val seek = mutableSetOf<Any>()
        lastId?.let { seek.add(it) }

        val query = ctx.select(
            ORDER_LOT.ID,
            ORDER_LOT.ANIMAL_ID,
            ORDER_LOT.PRICE,
            ORDER_LOT.CREATED_AT,
            ORDER_LOT.SUBWAY_ID,
            ORDER_LOT.TITLE,
            ORDER_LOT.SERVICE_DATE_FROM,
            ORDER_LOT.SERVICE_DATE_TO,
            ORDER_LOT.SERVICE_TYPE,
        )
            .from(ORDER_LOT)
            .where(*filterConditions.toTypedArray())
            .orderBy(sortConditions)
            .let {
                if (lastId != null) it.seek(*seek.toTypedArray()).limit(pageSize)
                else it.limit(pageSize)
            }

        logger.debug(append("query", query.toString())) { "Composed jooq query" }

        val result = Flux.from(query)
            .log()
            .map {
                it.into(OrderLotWrapper::class.java)
            }
            .collectList()
            .block() ?: emptyList()

        logger.debug(append("result", result.map { it.toString() })) { "Completed request" }

        return result
    }
}
