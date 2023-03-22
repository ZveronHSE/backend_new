package ru.zveron.test.util.model

import com.google.protobuf.timestamp
import org.jooq.Record
import org.jooq.SortField
import org.jooq.SortOrder
import org.jooq.TableField
import ru.zveron.contract.lot.*
import ru.zveron.contract.lot.model.Parameter
import ru.zveron.model.search.table.LOT
import java.time.Instant

object WaterfallEntities {
    fun mockWaterfallRequest(
        pageSize: Int = 10,
        isSortByDate: Boolean = false,
        typeSort: TypeSort = TypeSort.ASC,
        lotId: Long = 10L,
        lotValue: Int = 10000,
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
                    price = lotValue
                    date = timestamp {
                        seconds = lotValue.toLong()
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
            fieldName = LOT.PRICE

            if (sort.hasLastLot()) {
                conditionsSeek.addAll(
                    listOf(
                        sort.lastLot.price,
                        sort.lastLot.id,
                    )
                )
            }
        } else {
            fieldName = LOT.CREATED_AT

            if (sort.hasLastLot()) {
                conditionsSeek.addAll(
                    listOf(
                        Instant.ofEpochSecond(
                            sort.lastLot.date.seconds,
                            sort.lastLot.date.nanos.toLong()
                        ),
                        sort.lastLot.id,
                    )
                )
            }
        }

        val sortOrder = if (sort.typeSort == TypeSort.ASC) SortOrder.ASC else SortOrder.DESC


        return listOf(fieldName.sort(sortOrder), LOT.ID.sort(sortOrder)) to conditionsSeek
    }
}