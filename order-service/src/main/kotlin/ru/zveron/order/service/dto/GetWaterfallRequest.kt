package ru.zveron.order.service.dto

data class GetWaterfallRequest(
        val pageSize: Int,
        val lastOrderId: Long? = null,
        val sort: Sort,
        val filters: List<Filter> = emptyList(),
)

data class Filter(
        val field: Field,
        val operation: Operation,
        val value: String,
        val sort: Sort,
)

data class Sort(
        val sortBy: SortBy,
        val sortDirection: SortDirection,
)

enum class SortBy {
    DEFAULT,
    BY_DATE_CREATED,
    BY_PRICE,
    BY_DISTANCE,
}

enum class SortDirection {
    ASC,
    DESC,
}

enum class Operation {
    EQUAL,
    NOT_EQUAL,
    GREATER,
    GREATER_OR_EQUAL,
    LESS,
    LESS_OR_EQUAL,
    IN,
    NOT_IN,
    LIKE,
    NOT_LIKE,
    IS_NULL,
    IS_NOT_NULL
}

enum class Field {
    ID,
    PROFILE_ID,
    SUBWAY_ID,
    ANIMAL_ID,
    CREATED_AT,
    UPDATED_AT,
    STATUS,
    PRICE,
    DESCRIPTION,
    RATING
}
