package ru.zveron.order.service.constant

import org.jooq.Field
import org.jooq.SortField

enum class SortDirection(val dir: (Field<Any>) -> SortField<Any>) {
    ASC({ it.asc() }),
    DESC({ it.desc() }),
}