package ru.zveron.service

import org.springframework.stereotype.Service
import ru.zveron.repository.LotStatisticsRepository
import ru.zveron.util.ValidateUtils.validatePositive

@Service
class LotStatisticsService(
    private val lotStatisticsRepository: LotStatisticsRepository
) {
    fun incrementViewCounter(id: Long) {
        id.validatePositive("lotId")

        lotStatisticsRepository.incrementViewCounter(id)
    }
}