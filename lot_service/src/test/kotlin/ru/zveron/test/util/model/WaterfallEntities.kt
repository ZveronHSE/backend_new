package ru.zveron.test.util.model

import com.google.protobuf.timestamp
import org.jooq.Record
import org.jooq.SortField
import org.jooq.SortOrder
import org.jooq.TableField
import ru.zveron.contract.lot.Filter
import ru.zveron.contract.lot.SortByDateKt
import ru.zveron.contract.lot.SortByPriceKt
import ru.zveron.contract.lot.TypeSort
import ru.zveron.contract.lot.WaterfallRequest
import ru.zveron.contract.lot.model.Parameter
import ru.zveron.contract.lot.sortByDate
import ru.zveron.contract.lot.sortByPrice
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
            isSortByDate
                .takeIf { it }
                ?.let {
                    sortByDate = sortByDate {
                        this.typeSort = typeSort
                        lastLot = SortByDateKt.lastLot {
                            id = lotId
                            date = timestamp {
                                seconds = lotValue
                            }
                        }
                    }
                } ?: run {
                sortByPrice = sortByPrice {
                    this.typeSort = typeSort
                    lastLot = SortByPriceKt.lastLot {
                        id = lotId
                        price = lotValue.toInt()
                    }
                }
            }

            this.query = query
            this.parameters.addAll(parameters)
            this.filters.addAll(filters)
        }
    }

    /**
     * Return sorts to values for seek method from waterfallRequest
     */
    fun buildParametersForSeekMethod(waterfall: WaterfallRequest): Pair<List<SortField<*>>, MutableList<Any>> {
        val isAscendingOrder: Boolean
        val fieldName: TableField<Record, *>
        val conditionsSeek = mutableListOf<Any>()

        if (waterfall.sortCase == WaterfallRequest.SortCase.SORT_BY_DATE) {
            isAscendingOrder = waterfall.sortByDate.typeSort == TypeSort.ASC
            fieldName = LOT.DATE_CREATION
            conditionsSeek.addAll(
                listOf(
                    waterfall.sortByDate.lastLot.id,
                    waterfall.sortByDate.lastLot.date
                )
            )
        } else {
            isAscendingOrder = waterfall.sortByPrice.typeSort == TypeSort.ASC
            fieldName = LOT.PRICE
            conditionsSeek.addAll(
                listOf(
                    waterfall.sortByPrice.lastLot.id,
                    waterfall.sortByPrice.lastLot.price
                )
            )
        }

        val sortOrder = if (isAscendingOrder) SortOrder.ASC else SortOrder.DESC


        return listOf(LOT.ID.sort(sortOrder), fieldName.sort(sortOrder)) to conditionsSeek
    }
}