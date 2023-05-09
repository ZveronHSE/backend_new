package ru.zveron.repository.waterfall

import org.jooq.DSLContext
import org.jooq.SortField
import org.springframework.stereotype.Component
import ru.zveron.model.SummarySpecialist

@Component
class WaterfallRepository(
    private val context: DSLContext
) {
    fun getSpecialists(
        sorts: MutableList<SortField<*>>,
        values: MutableList<Any>,
        pageSize: Int
    ): List<SummarySpecialist> {
        val sql = context
            .select(
                SPECIALIST.ID,
                SPECIALIST.NAME,
                SPECIALIST.SURNAME,
                SPECIALIST.PATRONYMIC,
                SPECIALIST.IMG_URL,
                SPECIALIST.DESCRIPTION
            )
            .from(SPECIALIST)
            .orderBy(sorts)

        // Если запрашиваем первую страничку, то conditionsSeek нет.
        if (values.isEmpty()) {
            return sql.limit(pageSize).fetchInto(SummarySpecialist::class.java)
        }

        return sql
            .seek(*values.toTypedArray())
            .limit(pageSize)
            .fetchInto(SummarySpecialist::class.java)
    }
}