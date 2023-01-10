package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import ru.zveron.entity.LotStatistics

interface LotStatisticsRepository : JpaRepository<LotStatistics, Long> {
    @Modifying
    @Query("UPDATE LotStatistics l set l.quantity_favorite = l.quantity_favorite + 1 WHERE l.id = ?1")
    fun incrementFavoriteCounter(id: Long)

    @Modifying
    @Query(
        "UPDATE lot_statistics set quantity_favorite = GREATEST(quantity_favorite - 1, 0) WHERE lot_id = ?1",
        nativeQuery = true
    )
    fun decrementFavoriteCounter(id: Long)

    @Modifying
    @Query("UPDATE LotStatistics l set l.quantity_view = l.quantity_view + 1 WHERE l.id = ?1")
    fun incrementViewCounter(id: Long)
}