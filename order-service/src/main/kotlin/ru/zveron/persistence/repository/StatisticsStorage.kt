package ru.zveron.persistence.repository

import org.springframework.stereotype.Component
import ru.zveron.persistence.entity.Statistics

@Component
class StatisticsStorage(
    private val statisticsRepository: StatisticsRepository,
) {

    suspend fun incrementViewCount(orderLotId: Long) {
        val statistics = statisticsRepository.findByOrderLotId(orderLotId)

        statistics?.let {
            statisticsRepository.save(it.copy(viewCount = it.viewCount.inc()))
        } ?: run {
            statisticsRepository.save(Statistics(orderLotId = orderLotId, viewCount = 1))
        }
    }

    suspend fun getOrderLotToViewCount(orderLotIds: List<Long>): List<Statistics> {
        return statisticsRepository.findAllByOrderLotIdIn(orderLotIds)
    }
}