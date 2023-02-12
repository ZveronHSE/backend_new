package ru.zveron.model.search.table

import org.jooq.Record
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import org.jooq.impl.TableImpl

object LOT_PHOTO : TableImpl<Record>(DSL.name("lot_photo"), null) {
    /**
     * The column `public.lot_photo.id`.
     */
    val ID = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false), this, "")

    /**
     * The column `public.lot_photo.order_photo`.
     */
    val ORDER_PHOTO = createField(DSL.name("order_photo"), SQLDataType.INTEGER.nullable(false), this, "")

    /**
     * The column `public.lot_photo.id_lot`.
     */
    val ID_LOT = createField(DSL.name("lot_id"), SQLDataType.BIGINT.nullable(false), this, "")

    /**
     * The column `public.lot_photo.image_id`.
     */
    val IMAGE_ID = createField(DSL.name("image_id"), SQLDataType.BIGINT, this, "")

}