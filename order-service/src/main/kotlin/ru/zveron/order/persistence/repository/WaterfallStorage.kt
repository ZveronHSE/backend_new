package ru.zveron.order.persistence.repository

import kotlinx.coroutines.reactor.awaitSingle
import mu.KLogging
import net.logstash.logback.marker.Markers.append
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.SortField
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import ru.zveron.order.persistence.entity.OrderLot
import ru.zveron.order.persistence.jooq.models.ORDER_LOT
import ru.zveron.order.persistence.repository.model.JooqOrderLotQuery
import ru.zveron.order.persistence.repository.model.JooqOrderLotQueryBuilder
import ru.zveron.order.persistence.repository.model.OrderLotWrapper
import ru.zveron.order.service.constant.SortBy
import ru.zveron.order.service.constant.SortDirection
import ru.zveron.order.service.mapper.ModelMapper.toJooqFilter
import ru.zveron.order.service.model.Filter
import ru.zveron.order.service.model.Sort


@Component
class WaterfallStorage(
    private val ctx: DSLContext,
    private val repository: OrderLotRepository,
) {

    companion object : KLogging()

    suspend fun findAllPaginated(
        lastId: Long?,
        pageSize: Int,
        filters: List<Filter> = emptyList(),
        sort: Sort? = null,
    ): List<OrderLotWrapper> {

        //todo: should be whole entity coming from the client
        val lastEntity = lastId?.let {
            repository.findById(it)
        }

        val filterConditions = filters.map { it.toJooqFilter() }

        val jooqOrderLotQueryBuilder = JooqOrderLotQueryBuilder()

        val queryBuilder = applySort(jooqOrderLotQueryBuilder, sort, lastEntity)

        val jooqOrderLotQuery: JooqOrderLotQuery = queryBuilder
            .filterConditions(filterConditions)
            .pageSize(pageSize)
            .lastOrderLotId(lastId)
            .build()

        val query = jooqOrderLotQuery.getQuery(ctx)

        logger.debug(append("query", query.toString())) { "Composed jooq query" }

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

    @Suppress("UNCHECKED_CAST")
    private fun applySort(
        queryBuilder: JooqOrderLotQueryBuilder,
        sort: Sort?,
        lastEntity: OrderLot?,
    ): JooqOrderLotQueryBuilder {
        when (val sortByCasted = sort?.sortBy) {
            is SortBy.ByDistance -> {
                logger.debug(append("ids", sortByCasted.sortedIds)) { "Sorting by distance for ids" }

                val sortedSubwayIds =
                    sortByCasted.sortedIds.let { if (sort.sortDirection == SortDirection.DESC) it.reversed() else it }
                val sortField: SortField<Int> = ORDER_LOT.SUBWAY_ID
                    .sort(sortByCasted.sortedIds.associateWith { sortedSubwayIds.indexOf(it) })
                    .let {
                        if (sort.sortDirection == SortDirection.ASC) it.nullsFirst() else it.nullsLast()
                    }

                queryBuilder.addSortParam(sortField as SortField<Any>)
                lastEntity?.run {
                    queryBuilder.addSeekParam(sortedSubwayIds.indexOf(lastEntity.subwayId) as Any)
                }
            }

            is SortBy.ByPrice -> {
                queryBuilder.addSortParam(sort.sortDirection.dir(sortByCasted.priceField as Field<Any>))
                lastEntity?.run {
                    queryBuilder.addSeekParam(lastEntity.price as Any)
                }
            }

            is SortBy.ByServiceDate -> {
                queryBuilder.addSortParam(sort.sortDirection.dir(sortByCasted.dateFromField as Field<Any>))
                queryBuilder.addSortParam(sort.sortDirection.dir(sortByCasted.dateToField as Field<Any>))

                lastEntity?.run {
                    queryBuilder.addSeekParam(lastEntity.serviceDateFrom as Any)
                    queryBuilder.addSeekParam(lastEntity.serviceDateTo as Any)
                }
            }

            null -> {} //do nothing
        }

        return queryBuilder
    }
}
