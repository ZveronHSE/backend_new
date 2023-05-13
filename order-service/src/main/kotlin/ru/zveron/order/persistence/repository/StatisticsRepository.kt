package ru.zveron.order.persistence.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.zveron.order.persistence.entity.Statistics

@Suppress("SpringDataRepositoryMethodReturnTypeInspection")
interface StatisticsRepository : CoroutineCrudRepository<Statistics, Long> {

    suspend fun findByOrderLotId(orderLotId: Long): Statistics?

    suspend fun findAllByOrderLotIdIn(orderLotIds: List<Long>): List<Statistics>
}