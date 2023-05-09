package ru.zveron.repository.waterfall

import org.jooq.Record
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import org.jooq.impl.TableImpl

object SPECIALIST : TableImpl<Record>(DSL.name("specialist"), null) {

    val ID = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false), this, "")
    val NAME = createField(DSL.name("name"), SQLDataType.VARCHAR, this, "")
    val SURNAME = createField(DSL.name("surname"), SQLDataType.VARCHAR, this, "")
    val PATRONYMIC = createField(DSL.name("patronymic"), SQLDataType.VARCHAR, this, "")
    val IMG_URL = createField(DSL.name("img_url"), SQLDataType.VARCHAR, this, "")
    val DESCRIPTION = createField(DSL.name("description"), SQLDataType.VARCHAR, this, "")
}