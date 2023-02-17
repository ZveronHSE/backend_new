package ru.zveron.service.presentation.external

import com.google.protobuf.Empty
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
import ru.zveron.commons.generators.PrimitivesGenerator
import ru.zveron.commons.generators.LotsFavoritesRecordEntitiesGenerator
import ru.zveron.commons.generators.LotsFavoritesRecordEntitiesGenerator.generateLot
import ru.zveron.config.AuthorizedProfileElement
import ru.zveron.contract.lot.lotsIdResponse
import ru.zveron.exception.FavoritesException
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
        runBlocking(AuthorizedProfileElement(profileId1)) {
            lotsFavoriteService.addToFavorites(
                LotsFavoritesRecordEntitiesGenerator.createAddLotToFavoritesRequest(lotId1)
            )

            lotsFavoritesRecordRepository.findById(
                LotsFavoritesRecordEntitiesGenerator.generateKey(
                    profileId1,
                    lotId1
                )
            ).isPresent shouldBe true
        }
    }

    @Test
    fun `AddLotToFavorites When adds lot to favorites And it is already in favorites Then no exception is thrown`() {
        val (profileId1, lotId1) = PrimitivesGenerator.generateNIds(2)
        shouldNotThrow<FavoritesException> {
            runBlocking(AuthorizedProfileElement(profileId1)) {
                saveLotId(profileId1, lotId1)
                lotsFavoriteService.addToFavorites(
                    LotsFavoritesRecordEntitiesGenerator.createAddLotToFavoritesRequest(lotId1)
                )

                lotsFavoritesRecordRepository.findById(
                    LotsFavoritesRecordEntitiesGenerator.generateKey(
                        profileId1,
                        lotId1
                    )
                ).isPresent shouldBe true
            }
        }
    }

    @Test
    fun `AddLotToFavorites When unauthenticated`() {
        val (lotId1) = PrimitivesGenerator.generateNIds(1)
        val exception = shouldThrow<FavoritesException> {
            runBlocking {
                lotsFavoriteService.addToFavorites(
                    LotsFavoritesRecordEntitiesGenerator.createAddLotToFavoritesRequest(lotId1)
                )
            }
        }
        exception.message shouldBe "Authentication required"
    }

    @Test
    fun `RemoveLotFromFavorites When removes lot from favorites Then it is removed`() {
        val (profileId1, lotId1) = PrimitivesGenerator.generateNIds(2)
        runBlocking(AuthorizedProfileElement(profileId1)) {
            saveLotId(profileId1, lotId1)
            lotsFavoriteService.removeFromFavorites(
                LotsFavoritesRecordEntitiesGenerator.createRemoveLotFromFavoritesRequest(lotId1)
            )

            lotsFavoritesRecordRepository.findById(
                LotsFavoritesRecordEntitiesGenerator.generateKey(
                    profileId1,
                    lotId1
                )
            ).isPresent shouldBe false
        }
    }

    @Test
    fun `RemoveLotFromFavorites When removes not favorite lot from favorites Then got exception`() {
        val (profileId1, lotId1) = PrimitivesGenerator.generateNIds(2)
        val exception = shouldThrow<FavoritesException> {
            runBlocking(AuthorizedProfileElement(profileId1)) {
                saveLotId(profileId1, lotId1)
                removeLot(profileId1, lotId1)

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
        val exception = shouldThrow<FavoritesException> {
            runBlocking {
                lotsFavoriteService.removeFromFavorites(
                    LotsFavoritesRecordEntitiesGenerator.createRemoveLotFromFavoritesRequest(lotId1)
                )
            }
        }
        exception.message shouldBe "Authentication required"
    }

    @Test
    fun `GetFavoriteLots When requests for favorite lots Then appropriate lots are returned`() {
        val (profileId1, profileId2) = PrimitivesGenerator.generateNIds(2)
        val (lotId1, lotId2, lotId3) = PrimitivesGenerator.generateNIds(3)
        val expectedLots = listOf(generateLot(lotId1), generateLot(lotId2))
        coEvery { lotClient.getLotsById(listOf(lotId1, lotId2)) } returns lotsIdResponse {
            lots.addAll(
                expectedLots
            )
        }
        runBlocking(AuthorizedProfileElement(profileId1)) {
            saveLotId(profileId1, lotId1)
            saveLotId(profileId1, lotId2)
            saveLotId(profileId2, lotId2)
            saveLotId(profileId2, lotId3)

            val list = lotsFavoriteService.getFavoriteLots(Empty.getDefaultInstance())

            list.favoriteLotsList lotsShouldBe expectedLots
        }
    }

    @Test
    fun `GetFavoriteLots When unauthenticated`() {
        val exception = shouldThrow<FavoritesException> {
            runBlocking {
                lotsFavoriteService.getFavoriteLots(Empty.getDefaultInstance())
            }
        }
        exception.message shouldBe "Authentication required"
    }

    private fun saveLotId(profileId: Long, lotId: Long) = lotsFavoritesRecordRepository.save(
        LotsFavoritesRecordEntitiesGenerator.generateLotRecords(
            profileId,
            lotId
        )
    )

    private fun removeLot(ownerId: Long, lotId: Long) {
        transactionTemplate.execute {
            lotsFavoritesRecordRepository.delete(
                LotsFavoritesRecordEntitiesGenerator.generateLotRecords(
                    ownerId,
                    lotId
                )
            )
        }
    }
}