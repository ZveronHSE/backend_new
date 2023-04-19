package ru.zveron.order.persistence.jooq.models

import org.jooq.Record
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import org.jooq.impl.TableImpl

object ORDER_LOT: TableImpl<Record>(DSL.name("order_lot"), null) {

    val ID = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false), this, "")

    val SUBWAY_ID = createField(DSL.name("subway_id"), SQLDataType.INTEGER.nullable(false), this, "")

    val ANIMAL_ID = createField(DSL.name("animal_id"), SQLDataType.BIGINT.nullable(false), this, "")

    val CREATED_AT = createField(DSL.name("created_at"), SQLDataType.LOCALDATETIME(6), this, "")

    val PRICE = createField(DSL.name("price"), SQLDataType.VARCHAR, this, "")

    val TITLE = createField(DSL.name("title"), SQLDataType.VARCHAR(255), this, "")
}