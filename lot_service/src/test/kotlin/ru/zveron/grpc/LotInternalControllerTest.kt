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
import org.springframework.transaction.support.TransactionTemplate
import ru.zveron.DataBaseTest
import ru.zveron.client.favorite.LotFavoriteClient
import ru.zveron.contract.lot.LotsIdResponse
import ru.zveron.contract.lot.ProfileLotsResponse
import ru.zveron.contract.lot.lotsIdRequest
import ru.zveron.contract.lot.lotsIdResponse
import ru.zveron.contract.lot.profileLotsRequest
import ru.zveron.entity.Lot
import ru.zveron.entity.LotPhoto
import ru.zveron.exception.LotException
import ru.zveron.mapper.LotMapper
import ru.zveron.repository.LotRepository
import ru.zveron.test.util.GeneratorUtils.generateBooleans
import ru.zveron.test.util.GeneratorUtils.generateIds
import ru.zveron.test.util.GeneratorUtils.generateLong
import ru.zveron.test.util.model.LotEntities
import javax.transaction.Transactional


class LotInternalControllerTest : DataBaseTest() {
//    @Autowired
//    lateinit var lotInternalController: LotInternalController
//
//    @Autowired
//    lateinit var lotRepository: LotRepository
//
//    @MockkBean
//    lateinit var lotFavoriteClient: LotFavoriteClient
//
//    companion object {
//        const val QUANTITY_OF_LOTS = 5
//    }
//
//    @Autowired
//    lateinit var transactionTemplate: TransactionTemplate
//
//    @Test
//    fun `GetLotsById get corrects lots by ids without user`() {
////        var lots: List<Lot> = emptyList()
////        var expectedResponse: LotsIdResponse = LotsIdResponse.getDefaultInstance()
////        transactionTemplate.execute {
////            lots = lotRepository.saveAll(List(QUANTITY_OF_LOTS) { LotEntities.mockLotEntity() })
////            expectedResponse = LotMapper.buildLotsIdResponse(lots, favorites = null)
////        }
//        val (lots, expectedResponse) = runInTransaction(
//            { List(QUANTITY_OF_LOTS) { LotEntities.mockLotEntity() }},
//            {l -> LotMapper.buildLotsIdResponse(l, favorites = null)}
//        )
//
//        runBlocking {
//            val request = lotsIdRequest { lotIds.addAll(lots.map { it.id }) }
//
//            val actualResponse = lotInternalController.getLotsById(request)
//
//            actualResponse shouldBe expectedResponse
//            coVerify { lotFavoriteClient wasNot called }
//        }
//    }
//
//    @Test
//    fun `GetLotsById get corrects lots by ids with image`(): Unit = runBlocking {
//        val lot = LotEntities.mockLotEntity()
//        val photo = LotPhoto(lot = lot, imageUrl = "https://example.com", orderPhoto = 0)
//        lot.photos = listOf(photo)
//        val lots = listOf(lotRepository.save(lot))
//
//        val request = lotsIdRequest { lotIds.addAll(lots.map { it.id }) }
//
//        val expectedResponse = LotMapper.buildLotsIdResponse(lots, favorites = null)
//        val actualResponse = lotInternalController.getLotsById(request)
//
//        actualResponse shouldBe expectedResponse
//        coVerify { lotFavoriteClient wasNot called }
//    }
//
//
//    @Test
//    fun `GetLotsById get corrects lots by ids with user`(): Unit = runBlocking {
//        val userId = generateLong(start = 1)
//        val mockFavoritesRequest = generateBooleans(QUANTITY_OF_LOTS)
//
//        var lots: List<Lot> = emptyList()
//        var expectedResponse: LotsIdResponse = LotsIdResponse.getDefaultInstance()
//        transactionTemplate.execute {
//            lots = lotRepository.saveAll(List(QUANTITY_OF_LOTS) { LotEntities.mockLotEntity() })
//            expectedResponse = LotMapper.buildLotsIdResponse(lots, favorites = mockFavoritesRequest)
//        }
//
//        coEvery {
//            lotFavoriteClient.checkLotsAreFavorites(lots.map { it.id }, userId)
//        } returns mockFavoritesRequest
//
//        val request = lotsIdRequest {
//            lotIds.addAll(lots.map { it.id })
//            this.userId = userId
//        }
//        val actualResponse = lotInternalController.getLotsById(request)
//
//        actualResponse shouldBe expectedResponse
//    }
//
//    @Test
//    fun `GetLotsById return empty response, if get zero ids of lot`(): Unit = runBlocking {
//        val request = lotsIdRequest { }
//
//        val response = lotInternalController.getLotsById(request)
//
//        response shouldBe lotsIdResponse { }
//    }
//
//    @Test
//    fun `GetLotsBySellerId get response for correct request without user`(): Unit = runBlocking {
//        val sellerId = generateLong(start = 1)
//        var lots: List<Lot> = emptyList()
//        var expectedResponse: ProfileLotsResponse = ProfileLotsResponse.getDefaultInstance()
//        transactionTemplate.execute {
//            lots = lotRepository.saveAll(List(QUANTITY_OF_LOTS) { LotEntities.mockLotEntity(sellerId = sellerId) })
//                .sortedByDescending { it.createdAt }
//            expectedResponse = LotMapper.buildProfileLotsResponse(lots, favorites = null)
//        }
//        val actualResponse = lotInternalController.getLotsBySellerId(profileLotsRequest { this.sellerId = sellerId })
//
//        actualResponse shouldBe expectedResponse
//        coVerify { lotFavoriteClient wasNot called }
//    }
//
//
//    @Test
//    fun `GetLotsBySellerId get response for correct request with user`(): Unit = runBlocking {
//        val (sellerId, userId) = generateIds(2)
//        val mockFavoritesRequest = generateBooleans(QUANTITY_OF_LOTS)
//
//        val (lots, expectedResponse) = runInTransaction(
//            { List(QUANTITY_OF_LOTS) { LotEntities.mockLotEntity(sellerId = sellerId) }},
//            {l -> LotMapper.buildProfileLotsResponse(l, favorites = mockFavoritesRequest)}
//        )
//
//        coEvery {
//            lotFavoriteClient.checkLotsAreFavorites(lots.map { it.id }, userId)
//        } returns mockFavoritesRequest
//
//        val actualResponse = lotInternalController.getLotsBySellerId(profileLotsRequest {
//            this.sellerId = sellerId
//            this.userId = userId
//        })
//
//        actualResponse shouldBe expectedResponse
//    }
//
//    @Transactional
//    fun <T>runInTransaction(lots: () -> List<Lot>, resp: (List<Lot>) -> T): Pair<List<Lot>, T> {
//        val lots = lotRepository.saveAll(lots())
//        return lots to resp(lots)
//    }
//
//    @ParameterizedTest
//    @ValueSource(ints = [-10, -5, 0])
//    fun `GetLotsBySellerId should throw exception, if got negative seller id`(sellerId: Long): Unit = runBlocking {
//        shouldThrow<LotException> {
//            lotInternalController.getLotsBySellerId(profileLotsRequest { this.sellerId = sellerId })
//        }
//    }
}