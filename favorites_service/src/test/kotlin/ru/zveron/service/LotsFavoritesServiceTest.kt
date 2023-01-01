package ru.zveron.service

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomUtils
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionTemplate
import ru.zveron.FavoritesTest
import ru.zveron.commons.generators.IdsGenerator
import ru.zveron.commons.generators.LotsFavoritesCounterEntitiesGenerator.generateNShardsWithSumEqualsOne
import ru.zveron.commons.generators.LotsFavoritesRecordEntitiesGenerator.crateLotExistsInFavoritesRequest
import ru.zveron.commons.generators.LotsFavoritesRecordEntitiesGenerator.createAddLotToFavoritesRequest
import ru.zveron.commons.generators.LotsFavoritesRecordEntitiesGenerator.createListFavoritesLotsRequest
import ru.zveron.commons.generators.LotsFavoritesRecordEntitiesGenerator.createRemoveAllByFavoriteLotRequest
import ru.zveron.commons.generators.LotsFavoritesRecordEntitiesGenerator.createRemoveAllLotsByOwnerRequest
import ru.zveron.commons.generators.LotsFavoritesRecordEntitiesGenerator.createRemoveLotFromFavoritesRequest
import ru.zveron.commons.generators.LotsFavoritesRecordEntitiesGenerator.generateKey
import ru.zveron.commons.generators.LotsFavoritesRecordEntitiesGenerator.generateLotRecords
import ru.zveron.exception.FavoritesException
import ru.zveron.repository.LotsFavoritesCounterRepository
import ru.zveron.repository.LotsFavoritesRecordRepository

@Suppress("BlockingMethodInNonBlockingContext")
class LotsFavoritesServiceTest : FavoritesTest() {

    @Autowired
    lateinit var lotsFavoritesService: LotsFavoritesService

    @Autowired
    lateinit var lotsFavoritesRecordRepository: LotsFavoritesRecordRepository

    @Autowired
    lateinit var lotsFavoritesCounterRepository: LotsFavoritesCounterRepository

    @Autowired
    lateinit var transactionTemplate: TransactionTemplate

    @Test
    fun `AddLotToFavorites When adds lot to favorites first time Then it is added`() {
        val (profileId1, lotId1) = IdsGenerator.generateNIds(2)
        runBlocking {
            lotsFavoritesService.addLotToFavorites(
                createAddLotToFavoritesRequest(profileId1, lotId1)
            )

            lotsFavoritesRecordRepository.findById(generateKey(profileId1, lotId1)).isPresent shouldBe true
            lotsFavoritesCounterRepository.findAll().size shouldBe LotsFavoritesService.SHARDS_NUMBER
            lotsFavoritesCounterRepository.getLotFavoritesStatistics(lotId1) shouldBe 1
        }
    }

    @Test
    fun `AddLotToFavorites When adds lot to favorites And shards exist Then it is added`() {
        val (profileId1, profileId2, lotId1) = IdsGenerator.generateNIds(3)
        runBlocking {
            createLotsRecordWithShards(profileId2, lotId1)

            lotsFavoritesService.addLotToFavorites(
                createAddLotToFavoritesRequest(profileId1, lotId1)
            )

            lotsFavoritesRecordRepository.findById(generateKey(profileId1, lotId1)).isPresent shouldBe true
            lotsFavoritesCounterRepository.findAll().size shouldBe LotsFavoritesService.SHARDS_NUMBER
            lotsFavoritesCounterRepository.getLotFavoritesStatistics(lotId1) shouldBe 2
        }
    }

    @Test
    fun `AddLotToFavorites When adds lot to favorites And it is already in favorites Then no exception is thrown`() {
        val (profileId1, lotId1) = IdsGenerator.generateNIds(2)
        shouldNotThrow<FavoritesException> {
            runBlocking {
                createLotsRecordWithShards(profileId1, lotId1)

                lotsFavoritesService.addLotToFavorites(
                    createAddLotToFavoritesRequest(profileId1, lotId1)
                )

                lotsFavoritesRecordRepository.findById(generateKey(profileId1, lotId1)).isPresent shouldBe true
                lotsFavoritesCounterRepository.findAll().size shouldBe LotsFavoritesService.SHARDS_NUMBER
                lotsFavoritesCounterRepository.getLotFavoritesStatistics(lotId1) shouldBe 1
            }
        }
    }

    @Test
    fun `RemoveLotFromFavorites When removes lot from favorites Then it is removed`() {
        val (profileId1, lotId1) = IdsGenerator.generateNIds(2)
        runBlocking {
            createLotsRecordWithShards(profileId1, lotId1)
            lotsFavoritesService.removeLotFromFavorites(
                createRemoveLotFromFavoritesRequest(profileId1, lotId1)
            )

            lotsFavoritesRecordRepository.findById(generateKey(profileId1, lotId1)).isPresent shouldBe false
            lotsFavoritesCounterRepository.findAll().size shouldBe LotsFavoritesService.SHARDS_NUMBER
            lotsFavoritesCounterRepository.getLotFavoritesStatistics(lotId1) shouldBe 0
        }
    }

    @Test
    fun `RemoveLotFromFavorites When removes not favorite lot from favorites Then no exception is thrown`() {
        val (profileId1, lotId1) = IdsGenerator.generateNIds(2)
        shouldNotThrow<FavoritesException> {
            runBlocking {
                createLotsRecordWithShards(profileId1, lotId1)
                removeLot(profileId1, lotId1)

                lotsFavoritesService.removeLotFromFavorites(
                    createRemoveLotFromFavoritesRequest(profileId1, lotId1)
                )

                lotsFavoritesRecordRepository.findById(generateKey(profileId1, lotId1)).isPresent shouldBe false
                lotsFavoritesCounterRepository.findAll().size shouldBe LotsFavoritesService.SHARDS_NUMBER
                lotsFavoritesCounterRepository.getLotFavoritesStatistics(lotId1) shouldBe 0
            }
        }
    }

    @Test
    fun `LotExistsInFavorites When checks if lot exists in favorites And it is exists Then returns true`() {
        val (profileId1, lotId1) = IdsGenerator.generateNIds(2)
        runBlocking {
            createLotsRecordWithShards(profileId1, lotId1)
            val result = lotsFavoritesService.lotExistsInFavorites(
                crateLotExistsInFavoritesRequest(profileId1, lotId1)
            )

            result.lotExists shouldBe true
        }
    }

    @Test
    fun `LotExistsInFavorites When checks if lot exists in favorites And it don't exists Then returns false`() {
        val (profileId1, lotId1) = IdsGenerator.generateNIds(2)
        runBlocking {
            createLotsRecordWithShards(profileId1, lotId1)
            removeLot(profileId1, lotId1)
            val result = lotsFavoritesService.lotExistsInFavorites(
                crateLotExistsInFavoritesRequest(profileId1, lotId1)
            )

            result.lotExists shouldBe false
        }
    }

    @Test
    fun `ListFavoriteLots When requests for favorite lots Then appropriate lots are returned`() {
        val (profileId1, profileId2) = IdsGenerator.generateNIds(2)
        val (lotId1, lotId2, lotId3) = IdsGenerator.generateNIds(3)
        runBlocking {
            createLotsRecordWithShards(profileId1, lotId1)
            createLotsRecordWithShards(profileId1, lotId2)
            createLotsRecordWithShards(profileId2, lotId2)
            createLotsRecordWithShards(profileId2, lotId3)

            val list = lotsFavoritesService.listFavoriteLots(createListFavoritesLotsRequest(profileId1))

            list.favoriteLotsList.map { it.lotId }.shouldContainExactlyInAnyOrder(lotId1, lotId2)
        }
    }

    @Test
    fun `RemoveAllLotsByOwner When removes Than appropriate records should be removed`() {
        val (profileId1, profileId2) = IdsGenerator.generateNIds(2)
        val (lotId1, lotId2, lotId3) = IdsGenerator.generateNIds(3)
        runBlocking {
            createLotsRecordWithShards(profileId1, lotId1)
            createLotsRecordWithShards(profileId1, lotId2)
            createLotsRecordWithShards(profileId2, lotId2)
            createLotsRecordWithShards(profileId2, lotId3)

            lotsFavoritesService.removeAllLotsByOwner(createRemoveAllLotsByOwnerRequest(profileId1))

            val ids = lotsFavoritesRecordRepository.findAll().map { it.id.ownerUserId }.toSet()
            ids.shouldContainExactly(profileId2)
            lotsFavoritesCounterRepository.getLotFavoritesStatistics(lotId1) shouldBe 0
            lotsFavoritesCounterRepository.getLotFavoritesStatistics(lotId3) shouldBe 1
        }
    }

    @Test
    fun `RemoveAllByFavoriteLot When removes Than appropriate records should be removed`() {
        val (profileId1, profileId2) = IdsGenerator.generateNIds(2)
        val (lotId1, lotId2, lotId3) = IdsGenerator.generateNIds(3)
        runBlocking {
            createLotsRecordWithShards(profileId1, lotId1)
            createLotsRecordWithShards(profileId1, lotId2)
            createLotsRecordWithShards(profileId2, lotId2)
            createLotsRecordWithShards(profileId2, lotId3)

            lotsFavoritesService.removeAllByFavoriteLot(createRemoveAllByFavoriteLotRequest(lotId2))

            val ids = lotsFavoritesRecordRepository.findAll().map { it.id.favoriteLotId }.toSet()
            ids.shouldContainExactlyInAnyOrder(lotId1, lotId3)
            lotsFavoritesCounterRepository.getLotFavoritesStatistics(lotId2) shouldBe 0
            lotsFavoritesCounterRepository.findAll().size shouldBe 3 * LotsFavoritesService.SHARDS_NUMBER
        }
    }

    private fun createLotsRecordWithShards(ownerId: Long, lotId: Long) {
        lotsFavoritesRecordRepository.save(generateLotRecords(ownerId, lotId))
        lotsFavoritesCounterRepository.saveAll(
            generateNShardsWithSumEqualsOne(
                lotId,
                LotsFavoritesService.SHARDS_NUMBER
            )
        )
    }

    private fun removeLot(ownerId: Long, lotId: Long) {
        transactionTemplate.execute {
            lotsFavoritesRecordRepository.delete(generateLotRecords(ownerId, lotId))
            lotsFavoritesCounterRepository.decrementFavoriteCounter(
                lotId,
                RandomUtils.nextInt(0, LotsFavoritesService.SHARDS_NUMBER)
            )
        }
    }
}