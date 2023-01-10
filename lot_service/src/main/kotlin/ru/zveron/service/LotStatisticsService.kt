package ru.zveron.service

import org.springframework.stereotype.Service
import ru.zveron.repository.LotStatisticsRepository
import javax.transaction.Transactional

@Service
class LotStatisticsService(
    private val lotStatisticsRepository: LotStatisticsRepository
) {
    /**
     * Увеличивает счетчик добавлений в избранное на 1.
     * ВАЖНО: должен быть использован только внутри открытой транзакции
     */
    fun incrementFavoriteCounter(id: Long) = lotStatisticsRepository.incrementFavoriteCounter(id)

    /**
     * Уменьшает счетчик добавлений в избранное на 1.
     * ВАЖНО: должен быть использован только внутри открытой транзакции
     */
    fun decrementFavoriteCounter(id: Long) = lotStatisticsRepository.decrementFavoriteCounter(id)

    @Transactional
    fun incrementViewCounter(id: Long) = lotStatisticsRepository.incrementViewCounter(id)
}