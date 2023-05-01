package ru.zveron.order.persistence.jooq.models

import org.jooq.Record
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import org.jooq.impl.TableImpl

object ORDER_LOT : TableImpl<Record>(DSL.name("order_lot"), null) {

    val ID = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false), this, "")

    val SUBWAY_ID = createField(DSL.name("subway_id"), SQLDataType.INTEGER, this, "")

    val SUBWAY_ORDERING_ID = DSL.field("subway_ordering_id", SQLDataType.INTEGER)

    val ANIMAL_ID = createField(DSL.name("animal_id"), SQLDataType.BIGINT.nullable(false), this, "")

    val CREATED_AT = createField(DSL.name("created_at"), SQLDataType.LOCALDATETIME(6), this, "")

    val PRICE = createField(DSL.name("price"), SQLDataType.BIGINT, this, "")

    val TITLE = createField(DSL.name("title"), SQLDataType.VARCHAR(255), this, "")

    val SERVICE_DATE_FROM = createField(DSL.name("service_date_from"), SQLDataType.DATE, this, "")

    val SERVICE_DATE_TO = createField(DSL.name("service_date_to"), SQLDataType.DATE, this, "")

    val SERVICE_DELIVERY_TYPE = createField(DSL.name("service_delivery_type"), SQLDataType.VARCHAR, this, "")

    val SERVICE_TYPE = createField(DSL.name("service_type"), SQLDataType.VARCHAR, this, "")

    val STATUS = createField(DSL.name("status"), SQLDataType.VARCHAR, this, "")

}
