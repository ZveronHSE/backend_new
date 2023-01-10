package ru.zveron.mapper

import org.jooq.Condition
import org.jooq.Record
import org.jooq.SortOrder
import org.jooq.TableField
import org.jooq.impl.DSL
import ru.zveron.contract.lot.Field
import ru.zveron.contract.lot.Filter
import ru.zveron.contract.lot.Operation
import ru.zveron.contract.lot.TypeSort
import ru.zveron.contract.lot.WaterfallRequest
import ru.zveron.contract.lot.model.Parameter
import ru.zveron.model.constant.Gender
import ru.zveron.model.search.ConditionsSearch
import ru.zveron.model.search.ParametersSearch
import ru.zveron.model.search.ParametersSeekMethod
import ru.zveron.model.search.table.LOT

object ConditionsMapper {
    private const val SEPARATOR_VALUES = ";"

    fun parse(request: WaterfallRequest, categories: List<Int>?, sellerId: Long?): ConditionsSearch {
        val conditionsSearch = ConditionsSearch()

        // 1. Сортировка и пагинация с помощью seek method
        conditionsSearch.addSortAndPagination(request)

        // 2. Количество объявлений
        conditionsSearch.pageSize = request.pageSize

        // 3. Поиск по названию объявления
        if (request.query.isNotBlank()) {
            conditionsSearch.conditions.add(LOT.TITLE.likeIgnoreCase("%${request.query}%"))
        }

        // 4. Фильтрация по параметрам объявлений
        conditionsSearch.parameters = getParameters(request.parametersList)

        // 5. Группировка по категориям
        conditionsSearch.categories = categories

        // 6. Фильтрация по обычным полям объявлений
        conditionsSearch.conditions.addAll(request.filtersList.mapNotNull { getCondition(it) })

        // 7. Если пользователь авторизован, то убираем объявления, которые принадлежат ему, чтобы отображать только
        // собственные
        if (sellerId != null) {
            conditionsSearch.conditions.add(LOT.SELLER_ID.notEqual(sellerId))
        }

        return conditionsSearch
    }

    private fun getParameters(parameters: List<Parameter>): ParametersSearch? {
        // Так как для одного параметра может быть применено несколько значений, поэтому проверяем, сколько
        // параметров было задействовано через множество, исключая повторяющиеся случаи.
        val countOfParameters = hashSetOf<Int>()
        val parametersRow = parameters.map {
            countOfParameters.add(it.id)
            DSL.row(it.id, it.value)
        }

        return if (countOfParameters.size == 0) null else ParametersSearch(parametersRow, countOfParameters.size)
    }

    private fun getCondition(
        filter: Filter
    ): Condition? {
        val field = DSL.field(filter.field.name)
        val value = when (filter.operation) {
            Operation.IN, Operation.NOT_IN -> filter.value.split(SEPARATOR_VALUES)
                .map { castFieldValueToSpecificType(filter.field, it) }

            else -> castFieldValueToSpecificType(filter.field, filter.value)
        }

        return when (filter.operation) {
            Operation.NEGATION -> field.notEqual(value)
            Operation.GREATER_THAN -> field.greaterThan(value)
            Operation.GREATER_THAN_EQUALITY -> field.greaterOrEqual(value)
            Operation.LESS_THAN -> field.lessThan(value)
            Operation.LESS_THAN_EQUALITY -> field.lessOrEqual(value)
            Operation.IN -> field.`in`(value)
            Operation.NOT_IN -> field.notIn(value)
            Operation.EQUALITY -> field.eq(value)
            else -> null
        }
    }

    /**
     * Приводим к нужному типу значение.
     */
    private fun castFieldValueToSpecificType(field: Field, value: String): Any {
        return when (field) {
            Field.PRICE, Field.LOT_FORM_ID -> value.toLong()
            Field.GENDER -> Gender.valueOf(value).ordinal
//            Field.DATE_CREATION -> TODO парсилку
            else -> value
        }
    }


    /**
     * Получает сортировку одновременную по двум полям объявления, где первый параметр всегда ID объявления, а второй
     * параметр - по чему собираемся сортировать. Например: (id, price)
     *
     * Поддержаны следующие сортировки:
     * - Сортировка по цене (price)
     * - Сортировка по дате создания
     * - TODO Сортировка по рейтингу продавца ZV-300
     *
     * В зависимости от параметра в запросе [waterfallRequest] сортировка может быть
     * либо [SortOrder.ASC], либо [SortOrder.DESC].
     */
    private fun ConditionsSearch.addSortAndPagination(
        waterfallRequest: WaterfallRequest
    ) {
        val seekMethod = ParametersSeekMethod()
        var isAscendingOrder = false
        var fieldName: TableField<Record, *>? = null

        if (waterfallRequest.sortCase == WaterfallRequest.SortCase.SORT_BY_DATE) {
            isAscendingOrder = waterfallRequest.sortByDate.typeSort == TypeSort.ASC
            fieldName = LOT.DATE_CREATION

            if (waterfallRequest.sortByDate.hasLastLot()) {

                seekMethod.values.addAll(
                    listOf(
                        waterfallRequest.sortByDate.lastLot.id,
                        waterfallRequest.sortByDate.lastLot.date
                    )
                )
            }
        } else if (waterfallRequest.sortCase == WaterfallRequest.SortCase.SORT_BY_PRICE) {
            isAscendingOrder = waterfallRequest.sortByPrice.typeSort == TypeSort.ASC
            fieldName = LOT.PRICE

            if (waterfallRequest.sortByPrice.hasLastLot()) {
                seekMethod.values.addAll(
                    listOf(
                        waterfallRequest.sortByPrice.lastLot.id,
                        waterfallRequest.sortByPrice.lastLot.price
                    )
                )
            }
        }

        val sortOrder = if (isAscendingOrder) SortOrder.ASC else SortOrder.DESC

        seekMethod.sorts.addAll(
            listOf(
                LOT.ID.sort(sortOrder),
                fieldName!!.sort(sortOrder) // Всегда !!, тк сортировка всегда передана
            )
        )

        this.seekMethod = seekMethod
    }
}