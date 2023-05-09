package ru.zveron.repository.impl

import org.jooq.DSLContext
import org.jooq.impl.DSL.count
import org.jooq.impl.DSL.row
import org.springframework.stereotype.Component
import ru.zveron.model.SummaryLot
import ru.zveron.model.search.ConditionsSearch
import ru.zveron.model.search.ParametersSearch
import ru.zveron.model.search.table.LOT
import ru.zveron.model.search.table.LOT_PARAMETER
import ru.zveron.model.search.table.LOT_PHOTO
import ru.zveron.repository.WaterfallRepository

@Component
class WaterfallRepositoryImpl(
    private val context: DSLContext
) : WaterfallRepository {

    companion object {
        const val FIRST_IMAGE_ORDER = 0
    }

    override fun findAll(conditionsSearch: ConditionsSearch): List<SummaryLot> {
        val whereConditions = conditionsSearch.conditions
        if (!conditionsSearch.categories.isNullOrEmpty()) {
            whereConditions.add(LOT.CATEGORY_ID.`in`(conditionsSearch.categories))
        }

        // Искать объявления по определенным статусам
        whereConditions.add(LOT.STATUS.`in`(conditionsSearch.statuses))

        // Алиасы тут, поскольку из-за джойна были конфликты и скрипт не понимал, по какому столбцу нужно выполнять.
        var sql = context
            .select(
                LOT.ID,
                LOT.TITLE,
                LOT.PRICE,
                LOT.CREATED_AT.`as`("createdAt"),
                LOT_PHOTO.IMAGE_URL.`as`("imageUrl")
            )
            .from(LOT)
            .join(LOT_PHOTO)
            .on(LOT_PHOTO.ID_LOT.eq(LOT.ID)).and(LOT_PHOTO.ORDER_PHOTO.eq(FIRST_IMAGE_ORDER))
            .where(*whereConditions.toTypedArray())

        if (conditionsSearch.parameters != null) {
            sql = sql.and(LOT.ID.`in`(createSelectParameters(conditionsSearch.parameters!!)))
        }

        val sqlWithSorts = sql.orderBy(conditionsSearch.seekMethod.sorts)
        // Если запрашиваем первую страничку, то conditionsSeek нет.
        if (conditionsSearch.seekMethod.values.isEmpty()) {
            return sqlWithSorts.limit(conditionsSearch.pageSize).fetchInto(SummaryLot::class.java)
        }

        return sqlWithSorts
            .seek(*conditionsSearch.seekMethod.values.toTypedArray())
            .limit(conditionsSearch.pageSize)
            .fetchInto(SummaryLot::class.java)
    }


    private fun createSelectParameters(parametersSearch: ParametersSearch) =
        context.select(LOT_PARAMETER.ID_LOT)
            .from(LOT_PARAMETER)
            .where(row(LOT_PARAMETER.ID_PARAMETER, LOT_PARAMETER.VALUE).`in`(parametersSearch.parameters))
            .groupBy(LOT_PARAMETER.ID_LOT)
            .having(count().eq(parametersSearch.countOfFilters))
}