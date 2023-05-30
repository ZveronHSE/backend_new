package ru.zveron.service.constant

import org.jooq.Field
import org.jooq.SortField

enum class SortDirection(val sort: (Field<Any>) -> SortField<Any>) {
    ASC({ it.asc() }),
    DESC({ it.desc() }),
}
