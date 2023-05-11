package ru.zveron.order.service.constant

import org.jooq.Condition
import org.jooq.Record
import org.jooq.TableField
import ru.zveron.order.util.StringUtil.toList

enum class Operation(val operator: (tableField: TableField<Record, Any>, value: String) -> Condition) {
    EQUALITY({ field, value -> field.eq(value) }),
    NEGATION({ field, value -> field.ne(value) }),
    GREATER_THAN({ field, value -> field.gt(value) }),
    GREATER_THAN_EQUALITY({ field, value -> field.ge(value) }),
    LESS_THAN({ field, value -> field.lt(value) }),
    LESS_THAN_EQUALITY({ field, value -> field.le(value) }),
    IN({ field, value -> field.`in`(value.toList()) }),
    NOT_IN({ field, value -> field.notIn(value.toList()) }),
    LIKE({ field, value -> field.likeIgnoreCase(value) }),
}
