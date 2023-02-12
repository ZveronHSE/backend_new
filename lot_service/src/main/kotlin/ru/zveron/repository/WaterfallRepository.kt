package ru.zveron.repository

import org.springframework.data.repository.Repository
import ru.zveron.entity.Lot
import ru.zveron.model.SummaryLot
import ru.zveron.model.search.ConditionsSearch

interface WaterfallRepository : Repository<Lot, Long> {
    fun findAll(conditionsSearch: ConditionsSearch): List<SummaryLot>
}