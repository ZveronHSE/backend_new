package ru.zveron.service.presentation.external

import io.grpc.Status
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.transaction.support.TransactionTemplate
import ru.zveron.FavoritesTest
import ru.zveron.client.lot.LotClient
import ru.zveron.commons.assertions.LotAssertions.lotsShouldBe
import ru.zveron.commons.generators.LotsFavoritesRecordEntitiesGenerator
import ru.zveron.commons.generators.LotsFavoritesRecordEntitiesGenerator.generateLot
import ru.zveron.commons.generators.PrimitivesGenerator
import ru.zveron.contract.lot.lotsIdResponse
import ru.zveron.exception.FavoritesException
import ru.zveron.favorites.lot.LotStatus
import ru.zveron.favorites.lot.deleteAllByCategoryRequest
import ru.zveron.favorites.lot.deleteAllByStatusAndCategoryRequest
import ru.zveron.favorites.lot.getFavoriteLotsRequest
import ru.zveron.library.grpc.exception.PlatformException
import ru.zveron.library.grpc.interceptor.model.MetadataElement
import ru.zveron.library.grpc.model.Metadata
import ru.zveron.repository.LotsFavoritesRecordRepository

@Suppress("BlockingMethodInNonBlockingContext")
class LotFavoritesComponentInternalExternalExternalTest : FavoritesTest() {

    @Autowired
    lateinit var lotsFavoriteService: LotFavoritesGrpcServiceExternal

    @Autowired
    lateinit var lotsFavoritesRecordRepository: LotsFavoritesRecordRepository

    @Autowired
    lateinit var transactionTemplate: TransactionTemplate

    @TestConfiguration
    class InternalConfiguration {
        @Bean
        fun lotClient() = mockk<LotClient>()
    }

    @Autowired
    lateinit var lotClient: LotClient

    @Test
    fun `AddLotToFavorites When adds lot to favorites first time Then it is added`() {
        val (profileId1, lotId1) = PrimitivesGenerator.generateNIds(2)
        coEvery { lotClient.getLotsById(listOf(lotId1)) } returns lotsIdResponse {
            lots.addAll(listOf(generateLot(lotId1)))
        }
        runBlocking(MetadataElement(Metadata(profileId1))) {
            lotsFavoriteService.addToFavorites(
                LotsFavoritesRecordEntitiesGenerator.createAddLotToFavoritesRequest(lotId1)
            )

            lotsFavoritesRecordRepository.findById(
                LotsFavoritesRecordEntitiesGenerator.generateKey(
                    profileId1, lotId1
                )
            ).isPresent shouldBe true
        }
    }

    @Test
    fun `AddLotToFavorites When adds lot to favorites And it is already in favorites Then no exception is thrown`() {
        val (profileId1, lotId1) = PrimitivesGenerator.generateNIds(2)
        val categoryId = PrimitivesGenerator.generateCategoryId()
        coEvery { lotClient.getLotsById(listOf(lotId1)) } returns lotsIdResponse {
            lots.addAll(listOf(generateLot(lotId1)))
        }
        shouldNotThrow<FavoritesException> {
            runBlocking(MetadataElement(Metadata(profileId1))) {
                saveLotId(profileId1, lotId1, categoryId)
                lotsFavoriteService.addToFavorites(
                    LotsFavoritesRecordEntitiesGenerator.createAddLotToFavoritesRequest(lotId1)
                )

                lotsFavoritesRecordRepository.findById(
                    LotsFavoritesRecordEntitiesGenerator.generateKey(
                        profileId1, lotId1
                    )
                ).isPresent shouldBe true
            }
        }
    }

    @Test
    fun `AddLotToFavorites When unauthenticated`() {
        val (lotId1) = PrimitivesGenerator.generateNIds(1)
        val exception = shouldThrow<PlatformException> {
            runBlocking {
                lotsFavoriteService.addToFavorites(
                    LotsFavoritesRecordEntitiesGenerator.createAddLotToFavoritesRequest(lotId1)
                )
            }
        }
        exception.status shouldBe Status.UNAUTHENTICATED
        exception.message shouldBe "user should be authorized for this endpoint"
    }

    @Test
    fun `AddLotToFavorites When lot does not exists`() {
        val (profileId1, lotId1) = PrimitivesGenerator.generateNIds(2)
        coEvery { lotClient.getLotsById(listOf(lotId1)) } returns lotsIdResponse {}
        val exception = shouldThrow<FavoritesException> {
            runBlocking(MetadataElement(Metadata(profileId1))) {
                lotsFavoriteService.addToFavorites(
                    LotsFavoritesRecordEntitiesGenerator.createAddLotToFavoritesRequest(lotId1)
                )
            }
        }
        exception.status shouldBe Status.INVALID_ARGUMENT
        exception.message shouldBe "Объявления с id: $lotId1 не существует"
    }

    @Test
    fun `RemoveLotFromFavorites When removes lot from favorites Then it is removed`() {
        val (profileId1, lotId1) = PrimitivesGenerator.generateNIds(2)
        val categoryId = PrimitivesGenerator.generateCategoryId()
        runBlocking(MetadataElement(Metadata(profileId1))) {
            saveLotId(profileId1, lotId1, categoryId)
            lotsFavoriteService.removeFromFavorites(
                LotsFavoritesRecordEntitiesGenerator.createRemoveLotFromFavoritesRequest(lotId1)
            )

            lotsFavoritesRecordRepository.findById(
                LotsFavoritesRecordEntitiesGenerator.generateKey(
                    profileId1, lotId1
                )
            ).isPresent shouldBe false
        }
    }

    @Test
    fun `RemoveLotFromFavorites When removes not favorite lot from favorites Then got exception`() {
        val (profileId1, lotId1) = PrimitivesGenerator.generateNIds(2)
        val categoryId = PrimitivesGenerator.generateCategoryId()
        val exception = shouldThrow<FavoritesException> {
            runBlocking(MetadataElement(Metadata(profileId1))) {
                saveLotId(profileId1, lotId1, categoryId)
                removeLot(profileId1, lotId1, categoryId)

                lotsFavoriteService.removeFromFavorites(
                    LotsFavoritesRecordEntitiesGenerator.createRemoveLotFromFavoritesRequest(lotId1)
                )
            }
        }

        exception.message shouldBe "Нельзя удалить объявление не из списка избранного"
    }

    @Test
    fun `RemoveLotFromFavorites When unauthenticated`() {
        val (lotId1) = PrimitivesGenerator.generateNIds(1)
        val exception = shouldThrow<PlatformException> {
            runBlocking {
                lotsFavoriteService.removeFromFavorites(
                    LotsFavoritesRecordEntitiesGenerator.createRemoveLotFromFavoritesRequest(lotId1)
                )
            }
        }
        exception.status shouldBe Status.UNAUTHENTICATED
        exception.message shouldBe "user should be authorized for this endpoint"
    }

    @Test
    fun `GetFavoriteLots When requests for favorite lots Then appropriate lots are returned`() {
        val (profileId1, profileId2) = PrimitivesGenerator.generateNIds(2)
        val (lotId1, lotId2, lotId3) = PrimitivesGenerator.generateNIds(3)
        val categoryId = PrimitivesGenerator.generateCategoryId()
        val expectedLots = listOf(generateLot(lotId1), generateLot(lotId2))
        coEvery { lotClient.getLotsById(listOf(lotId1, lotId2)) } returns lotsIdResponse {
            lots.addAll(
                expectedLots
            )
        }
        runBlocking(MetadataElement(Metadata(profileId1))) {
            saveLotId(profileId1, lotId1, categoryId)
            saveLotId(profileId1, lotId2, categoryId)
            saveLotId(profileId2, lotId2, categoryId)
            saveLotId(profileId2, lotId3, categoryId)

            val list = lotsFavoriteService.getFavoriteLots(getFavoriteLotsRequest { this.categoryId = categoryId })

            list.favoriteLotsList lotsShouldBe expectedLots
        }
    }

    @Test
    fun `GetFavoriteLots When requests for favorite lots but with wrong category Then no lots are returned`() {
        val (profileId1, profileId2) = PrimitivesGenerator.generateNIds(2)
        val (lotId1, lotId2, lotId3) = PrimitivesGenerator.generateNIds(3)
        val (categoryId1, categoryId2) = PrimitivesGenerator.generateNCategoryIds(2)
        coEvery { lotClient.getLotsById(listOf()) } returns lotsIdResponse {}
        runBlocking(MetadataElement(Metadata(profileId1))) {
            saveLotId(profileId1, lotId1, categoryId1)
            saveLotId(profileId1, lotId2, categoryId1)
            saveLotId(profileId2, lotId2, categoryId1)
            saveLotId(profileId2, lotId3, categoryId1)

            val list = lotsFavoriteService.getFavoriteLots(getFavoriteLotsRequest { categoryId = categoryId2 })

            list.favoriteLotsList.isEmpty() shouldBe true
        }
    }

    @Test
    fun `GetFavoriteLots When unauthenticated`() {
        val categoryId = PrimitivesGenerator.generateCategoryId()
        val exception = shouldThrow<PlatformException> {
            runBlocking {
                lotsFavoriteService.getFavoriteLots(getFavoriteLotsRequest { this.categoryId = categoryId })
            }
        }
        exception.status shouldBe Status.UNAUTHENTICATED
        exception.message shouldBe "user should be authorized for this endpoint"
    }

    @Test
    fun `DeleteAllByCategory When deletes lots Then lots with appropriate category are deleted`() {
        val (profileId1, profileId2) = PrimitivesGenerator.generateNIds(2)
        val (lotId1, lotId2, lotId3) = PrimitivesGenerator.generateNIds(3)
        val (categoryId1, categoryId2) = PrimitivesGenerator.generateNCategoryIds(2)
        val expectedLots = listOf(generateLot(lotId1), generateLot(lotId2))
        coEvery { lotClient.getLotsById(listOf(lotId1, lotId2)) } returns lotsIdResponse {
            lots.addAll(
                expectedLots
            )
        }
        runBlocking(MetadataElement(Metadata(profileId1))) {
            saveLotId(profileId1, lotId1, categoryId1)
            val singleLot = saveLotId(profileId1, lotId2, categoryId2)
            saveLotId(profileId2, lotId2, categoryId1)
            saveLotId(profileId2, lotId3, categoryId1)

            lotsFavoriteService.deleteAllByCategory(deleteAllByCategoryRequest { categoryId = categoryId1 })

            lotsFavoritesRecordRepository.getAllById_OwnerUserIdAndCategoryId(profileId1, categoryId1)
                .isEmpty() shouldBe true
            lotsFavoritesRecordRepository.getAllById_OwnerUserIdAndCategoryId(profileId1, categoryId2)
                .first() shouldBe singleLot
        }
    }

    @Test
    fun `DeleteAllByCategory When unauthenticated`() {
        val categoryId = PrimitivesGenerator.generateCategoryId()
        val exception = shouldThrow<PlatformException> {
            runBlocking {
                lotsFavoriteService.deleteAllByCategory(deleteAllByCategoryRequest { this.categoryId = categoryId })
            }
        }
        exception.status shouldBe Status.UNAUTHENTICATED
        exception.message shouldBe "user should be authorized for this endpoint"
    }

    @Test
    fun `DeleteAllByStatus When deletes lots Then lots with appropriate status are deleted`() {
        val (profileId1, profileId2) = PrimitivesGenerator.generateNIds(2)
        val (lotId1, lotId2, lotId3, lotId4) = PrimitivesGenerator.generateNIds(4)
        val (categoryId1, categoryId2) = PrimitivesGenerator.generateNCategoryIds(2)
        val expectedLots = listOf(
            generateLot(lotId2, ru.zveron.contract.lot.model.Status.CLOSED),
            generateLot(lotId3, ru.zveron.contract.lot.model.Status.CLOSED),
            generateLot(lotId4),
        )
        coEvery { lotClient.getLotsById(listOf(lotId2, lotId3, lotId4)) } returns lotsIdResponse {
            lots.addAll(
                expectedLots
            )
        }
        runBlocking(MetadataElement(Metadata(profileId1))) {
            saveLotId(profileId1, lotId1, categoryId1)
            saveLotId(profileId1, lotId2, categoryId2)
            saveLotId(profileId1, lotId3, categoryId2)
            saveLotId(profileId1, lotId4, categoryId2)
            saveLotId(profileId2, lotId2, categoryId1)
            saveLotId(profileId2, lotId3, categoryId1)

            lotsFavoriteService.deleteAllByStatusAndCategory(deleteAllByStatusAndCategoryRequest {
                this.categoryId = categoryId2
                this.status = LotStatus.CLOSED
            })

            lotsFavoritesRecordRepository.existsById_OwnerUserIdAndId_FavoriteLotId(profileId1, lotId1) shouldBe true
            lotsFavoritesRecordRepository.existsById_OwnerUserIdAndId_FavoriteLotId(profileId1, lotId2) shouldBe false
            lotsFavoritesRecordRepository.existsById_OwnerUserIdAndId_FavoriteLotId(profileId1, lotId3) shouldBe false
            lotsFavoritesRecordRepository.existsById_OwnerUserIdAndId_FavoriteLotId(profileId1, lotId4) shouldBe true
            lotsFavoritesRecordRepository.existsById_OwnerUserIdAndId_FavoriteLotId(profileId2, lotId2) shouldBe true
            lotsFavoritesRecordRepository.existsById_OwnerUserIdAndId_FavoriteLotId(profileId2, lotId3) shouldBe true
        }
    }

    @Test
    fun `DeleteAllByStatus When unauthenticated`() {
        val categoryId = PrimitivesGenerator.generateCategoryId()
        val exception = shouldThrow<PlatformException> {
            runBlocking {
                lotsFavoriteService.deleteAllByStatusAndCategory(deleteAllByStatusAndCategoryRequest {
                    this.categoryId = categoryId
                    this.status = LotStatus.CLOSED
                })
            }
        }
        exception.status shouldBe Status.UNAUTHENTICATED
        exception.message shouldBe "user should be authorized for this endpoint"
    }

    private fun saveLotId(profileId: Long, lotId: Long, categoryId: Int) = lotsFavoritesRecordRepository.save(
        LotsFavoritesRecordEntitiesGenerator.generateLotRecords(
            profileId,
            lotId,
            categoryId,
        )
    )

    private fun removeLot(ownerId: Long, lotId: Long, categoryId: Int) {
        transactionTemplate.execute {
            lotsFavoritesRecordRepository.delete(
                LotsFavoritesRecordEntitiesGenerator.generateLotRecords(
                    ownerId,
                    lotId,
                    categoryId,
                )
            )
        }
    }
}