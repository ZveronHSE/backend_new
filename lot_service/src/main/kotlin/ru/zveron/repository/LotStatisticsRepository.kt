package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import ru.zveron.entity.LotStatistics

interface LotStatisticsRepository : JpaRepository<LotStatistics, Long> {
    @Modifying
    @Query("UPDATE LotStatistics l set l.quantityView = l.quantityView + 1 WHERE l.id = :id")
    fun incrementViewCounter(id: Long)
}