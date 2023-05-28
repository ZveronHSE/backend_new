package ru.zveron.entrpoint

import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import ru.zveron.contract.order.external.getFilteredCountRequest
import ru.zveron.contract.order.external.getWaterfallRequest
import ru.zveron.client.address.dto.GetSubwayStationApiResponse
import ru.zveron.client.animal.dto.GetAnimalApiResponse
import ru.zveron.client.profile.dto.GetProfileApiResponse
import ru.zveron.config.BaseOrderApplicationTest
import ru.zveron.persistence.model.constant.Status
import ru.zveron.test.util.testFindProfileResponse
import ru.zveron.test.util.testFullAnimal
import ru.zveron.test.util.testOrderLotEntity
import ru.zveron.test.util.testSubwayStation

class GetOrderWaterfallEntrypointTest @Autowired constructor(
    private val template: R2dbcEntityTemplate,
    private val entrypoint: GetOrderWaterfallEntrypoint,
) : BaseOrderApplicationTest() {

    @Test
    fun `given correct get request, when no last order id provided, then return list of waterfall orders`() {
        //prep data
        val request = getWaterfallRequest {
            this.pageSize = 5
        }

        val subway = testSubwayStation()
        val profileResponse = testFindProfileResponse()
        val subwayResponse = GetSubwayStationApiResponse.Success(subway)
        val animal = testFullAnimal()

        val orderEntities = List(10) { index -> testOrderLotEntity().copy(id = index.inc().toLong()) }

        //prep env
        runBlocking {
            orderEntities.map {
                template.insert(it).awaitSingleOrNull()?.id
            }
        }

        coEvery { subwayGrpcClient.getSubwayStation(any()) } returns subwayResponse
        coEvery { profileGrpcClient.getProfile(any()) } returns GetProfileApiResponse.Success(profileResponse)
        coEvery { animalGrpcClient.getAnimal(any()) } returns GetAnimalApiResponse.Success(animal)

        //when
        val response = runBlocking {
            entrypoint.getWaterfall(request)
        }

        //then
        val expectedOrders =
            orderEntities.filter { Status.canAcceptOrder(it.status) }.map { it.id }.sortedByDescending { it }.take(5)
        response.ordersCount shouldBe expectedOrders.size
        response.ordersList.map { it.id } shouldContainInOrder expectedOrders
    }

    @Test
    fun `given correct get count request, then return correct orders count`() {
        //prep data
        val request = getFilteredCountRequest { }

        val subway = testSubwayStation()
        val profileResponse = testFindProfileResponse()
        val subwayResponse = GetSubwayStationApiResponse.Success(subway)
        val animal = testFullAnimal()

        val orderEntities = List(10) { index -> testOrderLotEntity().copy(id = index.inc().toLong()) }

        //prep env
        runBlocking {
            orderEntities.map {
                template.insert(it).awaitSingleOrNull()?.id
            }
        }

        coEvery { subwayGrpcClient.getSubwayStation(any()) } returns subwayResponse
        coEvery { profileGrpcClient.getProfile(any()) } returns GetProfileApiResponse.Success(profileResponse)
        coEvery { animalGrpcClient.getAnimal(any()) } returns GetAnimalApiResponse.Success(animal)

        //when
        val response = runBlocking {
            entrypoint.getFilteredCount(request)
        }

        //then
        val expectedOrders =
            orderEntities.filter { Status.canAcceptOrder(it.status) }.map { it.id }.sortedByDescending { it }
        response.count shouldBe expectedOrders.size
    }
}
