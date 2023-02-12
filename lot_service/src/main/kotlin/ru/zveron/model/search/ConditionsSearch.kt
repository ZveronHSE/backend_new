package ru.zveron.model.search

import org.jooq.Condition
import org.jooq.SortField

data class ConditionsSearch(
    /**
     * Пагинация объявлений
     */
    var seekMethod: ParametersSeekMethod = ParametersSeekMethod(),

    /**
     * Количество объявлений
     */
    var pageSize: Int = 20,
    /**
     * Условия, по которым нужно будет фильтровать - больше или меньше, также
     */
    var conditions: MutableList<Condition> = mutableListOf(),
    /**
     * В каких категориях смотрим список объявлений.
     */
    var categories: List<Int>? = null,
    /**
     * Фильтрация по параметрам, которые есть у объявления
     */
    var parameters: ParametersSearch? = null,
)

data class ParametersSeekMethod(
    /**
     * По каким полям будем сортировать
     */
    val sorts: MutableList<SortField<*>> = mutableListOf(),
    /**
     * Условия, от какого поля будем запрашивать следующую страничку в пагинации
     */
    val values: MutableList<Any> = mutableListOf(),
)