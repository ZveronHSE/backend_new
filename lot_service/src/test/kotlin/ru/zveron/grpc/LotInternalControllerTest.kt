package ru.zveron.grpc

import com.ninjasquad.springmockk.MockkBean
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.called
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.DataBaseTest
import ru.zveron.client.favorite.LotFavoriteClient
import ru.zveron.contract.lot.lotsIdRequest
import ru.zveron.contract.lot.lotsIdResponse
import ru.zveron.contract.lot.profileLotsRequest
import ru.zveron.exception.LotException
import ru.zveron.mapper.LotMapper
import ru.zveron.repository.LotRepository
import ru.zveron.test.util.GeneratorUtils.generateBooleans
import ru.zveron.test.util.GeneratorUtils.generateIds
import ru.zveron.test.util.GeneratorUtils.generateLong
import ru.zveron.test.util.model.LotEntities


class LotInternalControllerTest : DataBaseTest() {
    @Autowired
    lateinit var lotInternalController: LotInternalController

    @Autowired
    lateinit var lotRepository: LotRepository

    @MockkBean
    lateinit var lotFavoriteClient: LotFavoriteClient

    companion object {
        const val QUANTITY_OF_LOTS = 5
    }

    @Test
    fun `GetLotsById get corrects lots by ids without user`(): Unit = runBlocking {
        val lots = lotRepository.saveAll(List(QUANTITY_OF_LOTS) { LotEntities.mockLotEntity() })

        val request = lotsIdRequest { lotIds.addAll(lots.map { it.id }) }

        val expectedResponse = LotMapper.buildLotsIdResponse(lots, favorites = null)
        val actualResponse = lotInternalController.getLotsById(request)

        actualResponse shouldBe expectedResponse
        coVerify { lotFavoriteClient wasNot called }
    }


    @Test
    fun `GetLotsById get corrects lots by ids with user`(): Unit = runBlocking {
        val userId = generateLong(start = 1)
        val lots = lotRepository.saveAll(List(QUANTITY_OF_LOTS) { LotEntities.mockLotEntity() })
        val mockFavoritesRequest = generateBooleans(QUANTITY_OF_LOTS)

        coEvery {
            lotFavoriteClient.checkLotsAreFavorites(lots.map { it.id }, userId)
        } returns mockFavoritesRequest

        val request = lotsIdRequest {
            lotIds.addAll(lots.map { it.id })
            this.userId = userId
        }

        val expectedResponse = LotMapper.buildLotsIdResponse(lots, favorites = mockFavoritesRequest)
        val actualResponse = lotInternalController.getLotsById(request)

        actualResponse shouldBe expectedResponse
    }

    @Test
    fun `GetLotsById return empty response, if get zero ids of lot`(): Unit = runBlocking {
        val request = lotsIdRequest { }

        val response = lotInternalController.getLotsById(request)

        response shouldBe lotsIdResponse { }
    }

    @Test
    fun `GetLotsBySellerId get response for correct request without user`(): Unit = runBlocking {
        val sellerId = generateLong(start = 1)
        val lots = lotRepository.saveAll(List(QUANTITY_OF_LOTS) { LotEntities.mockLotEntity(sellerId = sellerId) })
            .sortedByDescending { it.dateCreation }

        val expectedResponse = LotMapper.buildProfileLotsResponse(lots, favorites = null)
        val actualResponse = lotInternalController.getLotsBySellerId(profileLotsRequest { this.sellerId = sellerId })

        actualResponse shouldBe expectedResponse
        coVerify { lotFavoriteClient wasNot called }
    }


    @Test
    fun `GetLotsBySellerId get response for correct request with user`(): Unit = runBlocking {
        val (sellerId, userId) = generateIds(2)
        val lots = lotRepository.saveAll(List(QUANTITY_OF_LOTS) { LotEntities.mockLotEntity(sellerId = sellerId) })
            .sortedByDescending { it.dateCreation }
        val mockFavoritesRequest = generateBooleans(QUANTITY_OF_LOTS)

        coEvery {
            lotFavoriteClient.checkLotsAreFavorites(lots.map { it.id }, userId)
        } returns mockFavoritesRequest


        val expectedResponse = LotMapper.buildProfileLotsResponse(lots, favorites = mockFavoritesRequest)
        val actualResponse = lotInternalController.getLotsBySellerId(profileLotsRequest {
            this.sellerId = sellerId
            this.userId = userId
        })

        actualResponse shouldBe expectedResponse
    }

    @ParameterizedTest
    @ValueSource(ints = [-10, -5, 0])
    fun `GetLotsBySellerId should throw exception, if got negative seller id`(sellerId: Long): Unit = runBlocking {
        shouldThrow<LotException> {
            lotInternalController.getLotsBySellerId(profileLotsRequest { this.sellerId = sellerId })
        }
    }
}