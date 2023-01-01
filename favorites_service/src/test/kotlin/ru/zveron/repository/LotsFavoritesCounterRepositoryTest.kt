package ru.zveron.repository

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionTemplate
import ru.zveron.FavoritesTest
import ru.zveron.commons.assertions.LotsFavoritesCounterAssertions.assertLotStatistics
import ru.zveron.commons.assertions.LotsFavoritesCounterAssertions.assertShardsCounter
import ru.zveron.commons.generators.IdsGenerator.generateNIds
import ru.zveron.commons.generators.LotsFavoritesCounterEntitiesGenerator.generateCounterKey
import ru.zveron.commons.generators.LotsFavoritesCounterEntitiesGenerator.generateNShards
import ru.zveron.commons.generators.LotsFavoritesCounterEntitiesGenerator.generateNShardsWithIncreasingCounter
import ru.zveron.entity.LotsFavoritesCounter
import ru.zveron.service.LotsFavoritesService

@Suppress("BlockingMethodInNonBlockingContext")
class LotsFavoritesCounterRepositoryTest: FavoritesTest() {

    @Autowired
    lateinit var counterRepository: LotsFavoritesCounterRepository

    @Autowired
    lateinit var transactionTemplate: TransactionTemplate

    @Test
    fun `IncrementFavoriteCounter When increment Then appropriate shard is incremented`() {
        val (lot1Id, lot2Id) = generateNIds(2)
        runBlocking {
            generateNShards(lot1Id, LotsFavoritesService.SHARDS_NUMBER).forEach { counterRepository.save(it) }
            generateNShards(lot2Id, LotsFavoritesService.SHARDS_NUMBER).forEach { counterRepository.save(it) }

            val lot1Shard = counterRepository.findById(generateCounterKey(lot1Id, 0)).get()

            transactionTemplate.execute {
                counterRepository.incrementFavoriteCounter(lot1Shard.id.lotId, lot1Shard.id.shardId)
            }

            val updatedLot1Shard = counterRepository.findById(generateCounterKey(lot1Id, 0)).get()
            updatedLot1Shard.counter shouldBe lot1Shard.counter + 1
            counterRepository.assertShardsCounter(lot1Id, LotsFavoritesService.SHARDS_NUMBER, 0, setOf(lot1Shard.id.shardId))
            counterRepository.assertShardsCounter(lot2Id, LotsFavoritesService.SHARDS_NUMBER, 0)
        }
    }

    @Test
    fun `DecrementFavoriteCounter When increment Then appropriate shard is decremented`() {
        val (lot1Id, lot2Id) = generateNIds(2)
        runBlocking {
            generateNShards(lot1Id, LotsFavoritesService.SHARDS_NUMBER).forEach { counterRepository.save(it) }
            generateNShards(lot2Id, LotsFavoritesService.SHARDS_NUMBER).forEach { counterRepository.save(it) }

            val lot1Shard = counterRepository.save(LotsFavoritesCounter(generateCounterKey(lot1Id, 0), 1))

            transactionTemplate.execute {
                counterRepository.decrementFavoriteCounter(lot1Shard.id.lotId, lot1Shard.id.shardId)
            }

            val updatedLot1Shard = counterRepository.findById(generateCounterKey(lot1Id, 0)).get()
            updatedLot1Shard.counter shouldBe lot1Shard.counter - 1
            counterRepository.assertShardsCounter(lot1Id, LotsFavoritesService.SHARDS_NUMBER, 0, setOf(lot1Shard.id.shardId))
            counterRepository.assertShardsCounter(lot2Id, LotsFavoritesService.SHARDS_NUMBER, 0)
        }
    }

    @Test
    fun `GetLotFavoritesStatistics When shards exists Then the correct sum is returned`() {
        val (lot1Id, lot2Id) = generateNIds(2)
        runBlocking {
            generateNShardsWithIncreasingCounter(lot1Id, LotsFavoritesService.SHARDS_NUMBER).forEach { counterRepository.save(it) }
            generateNShardsWithIncreasingCounter(lot2Id, LotsFavoritesService.SHARDS_NUMBER).forEach { counterRepository.save(it) }

            counterRepository.assertLotStatistics(lot1Id)
            counterRepository.assertLotStatistics(lot2Id)
        }
    }

    @Test
    fun `ZeroAllLotShards When shards exists Then sum is equals zero`() {
        val (lot1Id, lot2Id) = generateNIds(2)
        runBlocking {
            generateNShardsWithIncreasingCounter(lot1Id, LotsFavoritesService.SHARDS_NUMBER).forEach { counterRepository.save(it) }
            generateNShardsWithIncreasingCounter(lot2Id, LotsFavoritesService.SHARDS_NUMBER).forEach { counterRepository.save(it) }

            transactionTemplate.execute {
                counterRepository.zeroAllLotShards(lot1Id)
            }

            counterRepository.assertShardsCounter(lot1Id, LotsFavoritesService.SHARDS_NUMBER, 0)
            counterRepository.assertLotStatistics(lot2Id)
        }
    }
}