package ru.zveron.commons.assertions

import io.kotest.matchers.shouldBe
import ru.zveron.entity.LotsFavoritesCounter
import ru.zveron.repository.LotsFavoritesCounterRepository
import ru.zveron.service.LotsFavoritesService

object LotsFavoritesCounterAssertions {

    fun LotsFavoritesCounterRepository.assertShardsCounter(
        lotId: Long,
        shardsSize: Int,
        counter: Long = 0,
        skipShards: Set<Int> = setOf(),
    ) =
        (0 until shardsSize).forEach { shardId ->
            if (shardId !in skipShards) {
                val shard = findById(LotsFavoritesCounter.LotsFavoritesCounterKey(lotId, shardId)).get()
                shard.counter shouldBe counter
            }
        }

    fun LotsFavoritesCounterRepository.assertLotStatistics(lotId: Long) =
        getLotFavoritesStatistics(lotId) shouldBe (0 until LotsFavoritesService.SHARDS_NUMBER).sum()
}