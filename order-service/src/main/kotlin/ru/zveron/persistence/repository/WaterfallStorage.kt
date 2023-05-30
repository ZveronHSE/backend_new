package ru.zveron.persistence.repository

import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingle
import mu.KLogging
import net.logstash.logback.marker.Markers.append
import net.logstash.logback.marker.Markers.appendRaw
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.SortField
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import ru.zveron.persistence.entity.OrderLot
import ru.zveron.persistence.jooq.models.ORDER_LOT
import ru.zveron.persistence.repository.model.JooqOrderLotQuery
import ru.zveron.persistence.repository.model.OrderLotWrapper
import ru.zveron.service.constant.SortBy
import ru.zveron.service.mapper.ModelMapper.toJooqFilter
import ru.zveron.service.model.FilterParam
import ru.zveron.service.model.Sort


@Component
class WaterfallStorage(
    private val ctx: DSLContext,
    private val repository: OrderLotRepository,
) {

    companion object : KLogging()

    suspend fun findAllPaginated(
        lastId: Long?,
        pageSize: Int,
        filterParams: List<FilterParam> = emptyList(),
        sort: Sort? = null,
    ): List<OrderLotWrapper> {

        //todo: should be whole entity coming from the client
        val lastEntity = lastId?.let {
            repository.findById(it)
        }

        val filterConditions = filterParams.map { it.toJooqFilter() }
        val sortParams = getSortParams(sort)
        val seekParams = lastEntity?.let { getSeekParams(sort, it) } ?: emptyList()

        val jooqQuery = JooqOrderLotQuery(
            seekParams = seekParams,
            filterConditions = filterConditions,
            sortParams = sortParams,
            pageSize = pageSize,
        )

        val query = jooqQuery.toSortedQuery(ctx)

        logger.debug(appendRaw("query", query.toString())) { "Composed jooq query" }

        val result = Flux.from(query)
            .log()
            .map { it.into(OrderLotWrapper::class.java) }
            .collectList()
            .awaitSingle() ?: emptyList()

        logger.debug(append("result", result.map { it.toString() })) { "Completed request" }

        return result
    }

    suspend fun countFiltered(filterParams: List<FilterParam> = emptyList()): Int {
        val filterConditions = filterParams.map { it.toJooqFilter() }

        val query = ctx.selectCount()
            .from(ORDER_LOT)
            .where(filterConditions)

        logger.debug(append("query", query.toString())) { "Composed jooq query" }

        val result: Int = Flux.from(query)
            .log()
            .map { it.into(Int::class.java) }
            .awaitSingle() ?: 0

        logger.debug(append("count", result)) { "Completed count request" }

        return result
    }

    @Suppress("UNCHECKED_CAST")
    private fun getSortParams(sort: Sort?): List<SortField<Any>> {
        return when (val sortCasted = sort?.sortBy) {
            is SortBy.ByDistance -> listOf(sortCasted.getSortField(sort.sortDirection) as SortField<Any>)
            is SortBy.ByPrice -> listOf(sort.sortDirection.sort(sortCasted.priceField as Field<Any>))
            is SortBy.ByServiceDate -> listOf(
                sort.sortDirection.sort(sortCasted.dateFromField as Field<Any>),
                sort.sortDirection.sort(sortCasted.dateToField as Field<Any>),
            )

            null -> emptyList()
        }.let {
            it + ORDER_LOT.ID.desc() as SortField<Any>
        }
    }

    private fun getSeekParams(sort: Sort?, lastEntity: OrderLot): List<Any> {
        return when (val sortCasted = sort?.sortBy) {
            is SortBy.ByDistance -> listOf(
                sortCasted.getSortedIds(sort.sortDirection).indexOf(lastEntity.subwayId) as Any
            )

            is SortBy.ByPrice -> listOf(lastEntity.price as Any)
            is SortBy.ByServiceDate -> listOf(lastEntity.serviceDateFrom as Any, lastEntity.serviceDateTo as Any)
            null -> emptyList()
        }.let {
            it + lastEntity.id as Any
        }
    }
}
