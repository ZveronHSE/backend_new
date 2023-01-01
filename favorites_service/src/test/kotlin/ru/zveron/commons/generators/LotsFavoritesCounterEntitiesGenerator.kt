package ru.zveron.commons.generators

import ru.zveron.entity.LotsFavoritesCounter

object LotsFavoritesCounterEntitiesGenerator {

    fun generateNShards(lotId: Long, n: Int) = List(n) { index -> generateCounter(lotId, index) }

    fun generateNShardsWithIncreasingCounter(lotId: Long, n: Int) = List(n) { index -> generateCounter(lotId, index, index.toLong()) }

    fun generateNShardsWithSumEqualsOne(lotId: Long, n: Int) = List(n) { index -> generateCounter(lotId, index, (index + 1L) / n) }

    fun generateCounter(lotId: Long, shardId: Int, counter: Long = 0) = LotsFavoritesCounter(generateCounterKey(lotId, shardId), counter)

    fun generateCounterKey(lotId: Long, shardId: Int) = LotsFavoritesCounter.LotsFavoritesCounterKey(lotId, shardId)
}