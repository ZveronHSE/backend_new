package ru.zveron.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.zveron.repository.LotStatisticsRepository
import ru.zveron.util.ValidateUtils.validatePositive

@Service
class LotStatisticsService(
    private val lotStatisticsRepository: LotStatisticsRepository
) {
    @Transactional
    fun incrementViewCounter(id: Long) {
        id.validatePositive("lotId")

        lotStatisticsRepository.incrementViewCounter(id)
    }
}