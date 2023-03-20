package ru.zveron.test.util.model

import com.google.protobuf.timestamp
import org.jooq.Record
import org.jooq.SortField
import org.jooq.SortOrder
import org.jooq.TableField
import ru.zveron.contract.lot.Filter
import ru.zveron.contract.lot.Sort
import ru.zveron.contract.lot.TypeSort
import ru.zveron.contract.lot.WaterfallRequest
import ru.zveron.contract.lot.lastLot
import ru.zveron.contract.lot.model.Parameter
import ru.zveron.contract.lot.sort
import ru.zveron.contract.lot.waterfallRequest
import ru.zveron.model.search.table.LOT

object WaterfallEntities {
    fun mockWaterfallRequest(
        pageSize: Int = 10,
        isSortByDate: Boolean = false,
        typeSort: TypeSort = TypeSort.ASC,
        lotId: Long = 10L,
        lotValue: Long = 10000L,
        query: String = "",
        parameters: List<Parameter> = listOf(),
        filters: List<Filter> = listOf()
    ): WaterfallRequest {
        return waterfallRequest {
            this.pageSize = pageSize
            sort = sort {
                this.typeSort = typeSort
                lastLot = lastLot {
                    id = lotId
                    price = lotValue.toInt()
                    date = timestamp {
                        seconds = lotValue
                    }
                }
                sortBy = if (isSortByDate) Sort.SortBy.DATE else Sort.SortBy.PRICE
            }

            this.query = query
            this.parameters.addAll(parameters)
            this.filters.addAll(filters)
        }
    }

    /**
     * Return sorts to values for seek method from waterfallRequest
     */
    fun buildParametersForSeekMethod(sort: Sort): Pair<List<SortField<*>>, MutableList<Any>> {
        val fieldName: TableField<Record, *>
        val conditionsSeek = mutableListOf<Any>()


        if (sort.sortBy == Sort.SortBy.PRICE) {
            fieldName = LOT.CREATED_AT

            if (sort.hasLastLot()) {
                conditionsSeek.addAll(
                    listOf(
                        sort.lastLot.id,
                        sort.lastLot.date
                    )
                )
            }
        } else {
            fieldName = LOT.PRICE

            if (sort.hasLastLot()) {
                conditionsSeek.addAll(
                    listOf(
                        sort.lastLot.id,
                        sort.lastLot.price
                    )
                )
            }
        }

        val sortOrder = if (sort.typeSort == TypeSort.ASC) SortOrder.ASC else SortOrder.DESC


        return listOf(LOT.ID.sort(sortOrder), fieldName.sort(sortOrder)) to conditionsSeek
    }
}