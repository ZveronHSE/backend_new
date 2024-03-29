package ru.zveron.model.search.table

import org.jooq.Record
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import org.jooq.impl.TableImpl


/**
 * This class is generated by jOOQ.
 */
object LOT : TableImpl<Record>(DSL.name("lot"), null) {

    /**
     * The column `public.lot.id`.
     */
    val ID = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false), this, "")

    /**
     * The column `public.lot.created_at`.
     */
    val CREATED_AT = createField(DSL.name("created_at"), SQLDataType.LOCALDATETIME(6), this, "")

    /**
     * The column `public.lot.description`.
     */
    val DESCRIPTION = createField(DSL.name("description"), SQLDataType.VARCHAR(255), this, "")

    /**
     * The column `public.lot.price`.
     */
    val PRICE = createField(DSL.name("price"), SQLDataType.INTEGER, this, "")

    /**
     * The column `public.lot.status`.
     */
    val STATUS = createField(DSL.name("status"), SQLDataType.VARCHAR, this, "")

    /**
     * The column `public.lot.title`.
     */
    val TITLE = createField(DSL.name("title"), SQLDataType.VARCHAR(255), this, "")

    /**
     * The column `public.lot.address_id`.
     */
    val ADDRESS_ID = createField(DSL.name("address_id"), SQLDataType.BIGINT.nullable(false), this, "")

    /**
     * The column `public.lot.category_id`.
     */
    val CATEGORY_ID = createField(DSL.name("category_id"), SQLDataType.BIGINT.nullable(false), this, "")

    /**
     * The column `public.lot.lot_form_id`.
     */
    val LOT_FORM_ID = createField(DSL.name("lot_form_id"), SQLDataType.BIGINT, this, "")

    /**
     * The column `public.lot.seller_id`.
     */
    val SELLER_ID = createField(DSL.name("seller_id"), SQLDataType.BIGINT, this, "")

    /**
     * The column `public.lot.ways_of_communicating`.
     */
    val WAYS_OF_COMMUNICATING = createField(DSL.name("ways_of_communicating"), SQLDataType.VARCHAR, this, "")

    /**
     * The column `public.lot.gender`.
     */
    val GENDER = createField(DSL.name("gender"), SQLDataType.INTEGER, this, "")
}
