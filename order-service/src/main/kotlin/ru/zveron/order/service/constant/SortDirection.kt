package ru.zveron.order.service.constant

import org.jooq.Record
import org.jooq.SortField
import org.jooq.TableField

enum class SortDirection(val sortOrder: (TableField<Record, Any>) -> SortField<Any>) {
    ASC({ it.asc() }),
    DESC({ it.desc() }),
}