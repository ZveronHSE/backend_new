package ru.zveron.client.favorite

import io.grpc.Status
import io.grpc.StatusException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import ru.zveron.exception.LotException
import ru.zveron.favorites.lot.LotFavoritesServiceGrpcKt
import ru.zveron.test.util.GeneratorUtils.generateIds
import ru.zveron.test.util.GeneratorUtils.generateInt
import ru.zveron.test.util.GeneratorUtils.generateLong
import ru.zveron.test.util.generator.FavoritesGenerator

@ExtendWith(MockKExtension::class)
class LotFavoriteClientTest {
    @InjectMockKs
    lateinit var lotFavoriteClient: LotFavoriteClient

    @MockK
    lateinit var lotFavoriteStub: LotFavoritesServiceGrpcKt.LotFavoritesServiceCoroutineStub


    @Test
    fun `CheckLotInFavorite get correct answer for correct request`(): Unit = runBlocking {
        val (lotId, userId) = generateIds()
        val response = FavoritesGenerator.generateLotExistInFavoritesResponse(1)
        coEvery {
            lotFavoriteStub.existInFavorites(any(), any())
        } returns response

        val actual = lotFavoriteClient.checkLotIsFavorite(lotId, userId)
        actual shouldBe response.isExistsList[0]
    }

    @Test
    fun `CheckLotInFavorite throw exception if got error from external`(): Unit = runBlocking {
        coEvery {
            lotFavoriteStub.existInFavorites(any(), any())
        } throws StatusException(Status.INTERNAL)

        val (lotId, userId) = generateIds()
        shouldThrow<LotException> { lotFavoriteClient.checkLotIsFavorite(lotId, userId) }
    }

    @Test
    fun `CheckLotInFavorite throw exception if got other quantity for lots from external`(): Unit = runBlocking {
        val (lotId, userId) = generateIds()
        val response = FavoritesGenerator.generateLotExistInFavoritesResponse(0)
        coEvery {
            lotFavoriteStub.existInFavorites(any(), any())
        } returns response

        shouldThrow<LotException> { lotFavoriteClient.checkLotIsFavorite(lotId, userId) }
    }

    @Test
    fun `CheckLotsInFavorite get correct answer for correct request`(): Unit = runBlocking {
        val size = generateInt()
        val lotIds = generateIds(size)
        val userId = generateLong()
        val response = FavoritesGenerator.generateLotExistInFavoritesResponse(size)

        coEvery {
            lotFavoriteStub.existInFavorites(any(), any())
        } returns response

        val actual = lotFavoriteClient.checkLotsAreFavorites(lotIds, userId)
        actual shouldContainInOrder response.isExistsList
    }

    @Test
    fun `CheckLotsInFavorite throw exception if got error from external`(): Unit = runBlocking {
        val lotIds = generateIds(5)
        val userId = generateLong()

        coEvery {
            lotFavoriteStub.existInFavorites(any(), any())
        } throws StatusException(Status.INTERNAL)


        shouldThrow<LotException> { lotFavoriteClient.checkLotsAreFavorites(lotIds, userId) }
    }

    @Test
    fun `CheckLotsInFavorite throw exception if got other quantity for lots from external`(): Unit = runBlocking {
        val size = generateInt()
        val lotIds = generateIds(size)
        val userId = generateLong()
        val response = FavoritesGenerator.generateLotExistInFavoritesResponse(size - 1)

        coEvery {
            lotFavoriteStub.existInFavorites(any(), any())
        } returns response


        shouldThrow<LotException> { lotFavoriteClient.checkLotsAreFavorites(lotIds, userId) }
    }
}