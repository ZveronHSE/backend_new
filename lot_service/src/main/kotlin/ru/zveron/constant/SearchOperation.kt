package ru.zveron.constant

enum class SearchOperation {
    EQUALITY, NEGATION, GREATER_THAN, GREATER_THAN_EQUALITY, LESS_THAN, LESS_THAN_EQUALITY, SORT_ASC,
    SORT_DESC, IN, NOT_IN, LIKE, PAIRWISE_COMPARISON;

    companion object {
        fun getSimpleOperation(input: String?): SearchOperation? {
            return when (input) {
                "=" -> EQUALITY
                "!" -> NEGATION
                ">" -> GREATER_THAN
                "<" -> LESS_THAN
                "<=" -> LESS_THAN_EQUALITY
                ">=" -> GREATER_THAN_EQUALITY
                ">>" -> SORT_ASC
                "<<" -> SORT_DESC
                "<>" -> PAIRWISE_COMPARISON
                "==" -> IN
                "!=" -> NOT_IN
                "LIKE" -> LIKE
                else -> null
            }
        }
    }
}