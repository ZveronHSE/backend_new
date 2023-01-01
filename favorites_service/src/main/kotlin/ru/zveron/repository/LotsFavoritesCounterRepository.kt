package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import ru.zveron.entity.LotsFavoritesCounter

interface LotsFavoritesCounterRepository: JpaRepository<LotsFavoritesCounter, LotsFavoritesCounter.LotsFavoritesCounterKey> {

    @Modifying
    @Query("UPDATE LotsFavoritesCounter l SET l.counter = l.counter + 1 WHERE l.id.lotId = ?1 AND l.id.shardId = ?2")
    fun incrementFavoriteCounter(lotId: Long, shardId: Int)

    @Modifying
    @Query("UPDATE LotsFavoritesCounter l SET l.counter = l.counter - 1 WHERE l.id.lotId = ?1 AND l.id.shardId = ?2")
    fun decrementFavoriteCounter(lotId: Long, shardId: Int)

    @Query("SELECT SUM(l.counter) FROM LotsFavoritesCounter l WHERE l.id.lotId = ?1")
    fun getLotFavoritesStatistics(lotId: Long): Long

    @Modifying
    @Query("UPDATE LotsFavoritesCounter l SET l.counter = 0 WHERE l.id.lotId = ?1")
    fun zeroAllLotShards(lotId: Long)

    fun existsById_LotId(lotId: Long): Boolean
}