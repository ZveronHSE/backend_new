package ru.zveron.service

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionTemplate
import ru.zveron.FavoritesTest
import ru.zveron.commons.generators.IdsGenerator
import ru.zveron.commons.generators.LotsFavoritesRecordEntitiesGenerator.crateLotExistsInFavoritesRequest
import ru.zveron.commons.generators.LotsFavoritesRecordEntitiesGenerator.createAddLotToFavoritesRequest
import ru.zveron.commons.generators.LotsFavoritesRecordEntitiesGenerator.createListFavoritesLotsRequest
import ru.zveron.commons.generators.LotsFavoritesRecordEntitiesGenerator.createRemoveAllByFavoriteLotRequest
import ru.zveron.commons.generators.LotsFavoritesRecordEntitiesGenerator.createRemoveAllLotsByOwnerRequest
import ru.zveron.commons.generators.LotsFavoritesRecordEntitiesGenerator.createRemoveLotFromFavoritesRequest
import ru.zveron.commons.generators.LotsFavoritesRecordEntitiesGenerator.generateKey
import ru.zveron.commons.generators.LotsFavoritesRecordEntitiesGenerator.generateLotRecords
import ru.zveron.exception.FavoritesException
import ru.zveron.repository.LotsFavoritesRecordRepository

@Suppress("BlockingMethodInNonBlockingContext")
class LotsFavoritesServiceTest : FavoritesTest() {

    @Autowired
    lateinit var lotsFavoritesService: LotsFavoritesService

    @Autowired
    lateinit var lotsFavoritesRecordRepository: LotsFavoritesRecordRepository

    @Autowired
    lateinit var transactionTemplate: TransactionTemplate

    @Test
    fun `AddLotToFavorites When adds lot to favorites first time Then it is added`() {
        val (profileId1, lotId1) = IdsGenerator.generateNIds(2)
        runBlocking {
            lotsFavoritesService.addToFavorites(
                createAddLotToFavoritesRequest(profileId1, lotId1)
            )

            lotsFavoritesRecordRepository.findById(generateKey(profileId1, lotId1)).isPresent shouldBe true
        }
    }

    @Test
    fun `AddLotToFavorites When adds lot to favorites And it is already in favorites Then no exception is thrown`() {
        val (profileId1, lotId1) = IdsGenerator.generateNIds(2)
        shouldNotThrow<FavoritesException> {
            runBlocking {
                lotsFavoritesRecordRepository.save(generateLotRecords(profileId1, lotId1))

                lotsFavoritesService.addToFavorites(
                    createAddLotToFavoritesRequest(profileId1, lotId1)
                )

                lotsFavoritesRecordRepository.findById(generateKey(profileId1, lotId1)).isPresent shouldBe true
            }
        }
    }

    @Test
    fun `RemoveLotFromFavorites When removes lot from favorites Then it is removed`() {
        val (profileId1, lotId1) = IdsGenerator.generateNIds(2)
        runBlocking {
            lotsFavoritesRecordRepository.save(generateLotRecords(profileId1, lotId1))

            lotsFavoritesService.removeFromFavorites(
                createRemoveLotFromFavoritesRequest(profileId1, lotId1)
            )

            lotsFavoritesRecordRepository.findById(generateKey(profileId1, lotId1)).isPresent shouldBe false
        }
    }

    @Test
    fun `RemoveLotFromFavorites When removes not favorite lot from favorites Then got exception`() {
        val (profileId1, lotId1) = IdsGenerator.generateNIds(2)
        val exception = shouldThrow<FavoritesException> {
            runBlocking {
                lotsFavoritesRecordRepository.save(generateLotRecords(profileId1, lotId1))
                removeLot(profileId1, lotId1)

                lotsFavoritesService.removeFromFavorites(
                    createRemoveLotFromFavoritesRequest(profileId1, lotId1)
                )
            }
        }

        exception.message shouldBe "Нельзя удалить объявление не из списка избранного"
    }

    @Test
    fun `LotExistsInFavorites When checks if lot exists in favorites Then returns correct results`() {
        val (profileId1, lotId1, lotId2, lotId3) = IdsGenerator.generateNIds(4)
        runBlocking {
            lotsFavoritesRecordRepository.save(generateLotRecords(profileId1, lotId1))
            lotsFavoritesRecordRepository.save(generateLotRecords(profileId1, lotId3))

            val result = lotsFavoritesService.existInFavorites(
                crateLotExistsInFavoritesRequest(profileId1, listOf(lotId1, lotId2, lotId3))
            )

            result.isExistsList.shouldContainExactly(true, false, true)
        }
    }

    @Test
    fun `ListFavoriteLots When requests for favorite lots Then appropriate lots are returned`() {
        val (profileId1, profileId2) = IdsGenerator.generateNIds(2)
        val (lotId1, lotId2, lotId3) = IdsGenerator.generateNIds(3)
        runBlocking {
            lotsFavoritesRecordRepository.save(generateLotRecords(profileId1, lotId1))
            lotsFavoritesRecordRepository.save(generateLotRecords(profileId1, lotId2))
            lotsFavoritesRecordRepository.save(generateLotRecords(profileId2, lotId2))
            lotsFavoritesRecordRepository.save(generateLotRecords(profileId2, lotId3))

            val list = lotsFavoritesService.getFavoriteLots(createListFavoritesLotsRequest(profileId1))

            list.favoriteLotsList.map { it.id }.shouldContainExactlyInAnyOrder(lotId1, lotId2)
        }
    }

    @Test
    fun `RemoveAllLotsByOwner When removes Than appropriate records should be removed`() {
        val (profileId1, profileId2) = IdsGenerator.generateNIds(2)
        val (lotId1, lotId2, lotId3) = IdsGenerator.generateNIds(3)
        runBlocking {
            lotsFavoritesRecordRepository.save(generateLotRecords(profileId1, lotId1))
            lotsFavoritesRecordRepository.save(generateLotRecords(profileId1, lotId2))
            lotsFavoritesRecordRepository.save(generateLotRecords(profileId2, lotId2))
            lotsFavoritesRecordRepository.save(generateLotRecords(profileId2, lotId3))

            lotsFavoritesService.removeAllByOwner(createRemoveAllLotsByOwnerRequest(profileId1))

            val favoritesRecords = lotsFavoritesRecordRepository.findAll()
            favoritesRecords.size shouldBe 2
            favoritesRecords.map { it.id.ownerUserId }.toSet().shouldContainExactly(profileId2)
        }
    }

    @Test
    fun `RemoveAllByFavoriteLot When removes Than appropriate records should be removed`() {
        val (profileId1, profileId2) = IdsGenerator.generateNIds(2)
        val (lotId1, lotId2, lotId3) = IdsGenerator.generateNIds(3)
        runBlocking {
            lotsFavoritesRecordRepository.save(generateLotRecords(profileId1, lotId1))
            lotsFavoritesRecordRepository.save(generateLotRecords(profileId1, lotId2))
            lotsFavoritesRecordRepository.save(generateLotRecords(profileId2, lotId2))
            lotsFavoritesRecordRepository.save(generateLotRecords(profileId2, lotId3))

            lotsFavoritesService.removeAllByFavoriteLot(createRemoveAllByFavoriteLotRequest(lotId2))

            val ids = lotsFavoritesRecordRepository.findAll().map { it.id.favoriteLotId }
            ids.shouldContainExactlyInAnyOrder(lotId1, lotId3)
        }
    }

    private fun removeLot(ownerId: Long, lotId: Long) {
        transactionTemplate.execute {
            lotsFavoritesRecordRepository.delete(generateLotRecords(ownerId, lotId))
        }
    }
}
