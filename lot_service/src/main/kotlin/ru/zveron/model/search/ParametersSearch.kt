package ru.zveron.model.search

import org.jooq.Row2

data class ParametersSearch(
    var parameters: List<Row2<Int, String>>,
    var countOfFilters: Int
)