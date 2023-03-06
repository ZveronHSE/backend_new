package ru.zveron.service.presentation.internal

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.FavoritesTest
import ru.zveron.commons.generators.PrimitivesGenerator
import ru.zveron.commons.generators.LotsFavoritesRecordEntitiesGenerator
import ru.zveron.favorites.lot.getLotCounterRequest
import ru.zveron.repository.LotsFavoritesRecordRepository

@Suppress("BlockingMethodInNonBlockingContext")
class LotFavoritesComponentInternalExternalInternalTest : FavoritesTest() {

    @Autowired
    lateinit var lotFavoritesGrpcServiceInternal: LotFavoritesGrpcServiceInternal

    @Autowired
    lateinit var lotsFavoritesRecordRepository: LotsFavoritesRecordRepository

    @Test
    fun `LotExistsInFavorites When checks if lot exists in favorites Then returns correct results`() {
        val (profileId1, lotId1, lotId2, lotId3) = PrimitivesGenerator.generateNIds(4)
        val categoryId = PrimitivesGenerator.generateCategoryId()
        runBlocking {
            lotsFavoritesRecordRepository.save(
                LotsFavoritesRecordEntitiesGenerator.generateLotRecords(
                    profileId1,
                    lotId1,
                    categoryId,
                )
            )
            lotsFavoritesRecordRepository.save(
                LotsFavoritesRecordEntitiesGenerator.generateLotRecords(
                    profileId1,
                    lotId3,
                    categoryId,
                )
            )

            val result = lotFavoritesGrpcServiceInternal.existInFavorites(
                LotsFavoritesRecordEntitiesGenerator.crateLotExistsInFavoritesRequest(
                    profileId1,
                    listOf(lotId1, lotId2, lotId3)
                )
            )

            result.isExistsList.shouldContainExactly(true, false, true)
        }
    }

    @Test
    fun `RemoveAllLotsByOwner When removes Than appropriate records should be removed`() {
        val (profileId1, profileId2) = PrimitivesGenerator.generateNIds(2)
        val (lotId1, lotId2, lotId3) = PrimitivesGenerator.generateNIds(3)
        val categoryId = PrimitivesGenerator.generateCategoryId()
        runBlocking {
            lotsFavoritesRecordRepository.save(
                LotsFavoritesRecordEntitiesGenerator.generateLotRecords(
                    profileId1,
                    lotId1,
                    categoryId,
                )
            )
            lotsFavoritesRecordRepository.save(
                LotsFavoritesRecordEntitiesGenerator.generateLotRecords(
                    profileId1,
                    lotId2,
                    categoryId,
                )
            )
            lotsFavoritesRecordRepository.save(
                LotsFavoritesRecordEntitiesGenerator.generateLotRecords(
                    profileId2,
                    lotId2,
                    categoryId,
                )
            )
            lotsFavoritesRecordRepository.save(
                LotsFavoritesRecordEntitiesGenerator.generateLotRecords(
                    profileId2,
                    lotId3,
                    categoryId,
                )
            )

            lotFavoritesGrpcServiceInternal.removeAllByOwner(
                LotsFavoritesRecordEntitiesGenerator.createRemoveAllLotsByOwnerRequest(
                    profileId1
                )
            )

            val favoritesRecords = lotsFavoritesRecordRepository.findAll()
            favoritesRecords.size shouldBe 2
            favoritesRecords.map { it.id.ownerUserId }.toSet().shouldContainExactly(profileId2)
        }
    }

    @Test
    fun `RemoveAllByFavoriteLot When removes Than appropriate records should be removed`() {
        val (profileId1, profileId2) = PrimitivesGenerator.generateNIds(2)
        val (lotId1, lotId2, lotId3) = PrimitivesGenerator.generateNIds(3)
        val categoryId = PrimitivesGenerator.generateCategoryId()
        runBlocking {
            lotsFavoritesRecordRepository.save(
                LotsFavoritesRecordEntitiesGenerator.generateLotRecords(
                    profileId1,
                    lotId1,
                    categoryId,
                )
            )
            lotsFavoritesRecordRepository.save(
                LotsFavoritesRecordEntitiesGenerator.generateLotRecords(
                    profileId1,
                    lotId2,
                    categoryId,
                )
            )
            lotsFavoritesRecordRepository.save(
                LotsFavoritesRecordEntitiesGenerator.generateLotRecords(
                    profileId2,
                    lotId2,
                    categoryId,
                )
            )
            lotsFavoritesRecordRepository.save(
                LotsFavoritesRecordEntitiesGenerator.generateLotRecords(
                    profileId2,
                    lotId3,
                    categoryId,
                )
            )

            lotFavoritesGrpcServiceInternal.removeAllByFavoriteLot(
                LotsFavoritesRecordEntitiesGenerator.createRemoveAllByFavoriteLotRequest(
                    lotId2
                )
            )

            val ids = lotsFavoritesRecordRepository.findAll().map { it.id.favoriteLotId }
            ids.shouldContainExactlyInAnyOrder(lotId1, lotId3)
        }
    }

    @Test
    fun `GetCounter When request is correct`() {
        val (profileId1, profileId2, lotId1, lotId2) = PrimitivesGenerator.generateNIds(4)
        val categoryId = PrimitivesGenerator.generateCategoryId()
        runBlocking {
            lotsFavoritesRecordRepository.save(
                LotsFavoritesRecordEntitiesGenerator.generateLotRecords(
                    profileId1,
                    lotId1,
                    categoryId,
                )
            )
            lotsFavoritesRecordRepository.save(
                LotsFavoritesRecordEntitiesGenerator.generateLotRecords(
                    profileId2,
                    lotId1,
                    categoryId,
                )
            )
            lotsFavoritesRecordRepository.save(
                LotsFavoritesRecordEntitiesGenerator.generateLotRecords(
                    profileId2,
                    lotId2,
                    categoryId,
                )
            )

            val result = lotFavoritesGrpcServiceInternal.getCounter(getLotCounterRequest { id = lotId1 })

            result.addsToFavoritesCounter shouldBe 2
        }
    }
}