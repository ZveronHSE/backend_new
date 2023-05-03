package ru.zveron.order.entrpoint

import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import ru.zveron.contract.order.external.getWaterfallRequest
import ru.zveron.order.client.address.dto.GetSubwayStationApiResponse
import ru.zveron.order.client.animal.dto.GetAnimalApiResponse
import ru.zveron.order.client.profile.dto.GetProfileApiResponse
import ru.zveron.order.config.BaseOrderApplicationTest
import ru.zveron.order.persistence.model.constant.Status
import ru.zveron.order.test.util.testFindProfileResponse
import ru.zveron.order.test.util.testFullAnimal
import ru.zveron.order.test.util.testOrderLotEntity
import ru.zveron.order.test.util.testSubwayStation

class OrderWaterfallServiceEntrypointTest @Autowired constructor(
        private val template: R2dbcEntityTemplate,
        private val entrypoint: OrderWaterfallServiceEntrypoint,
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
        val expectedOrders = orderEntities.filter { Status.canAcceptOrder(it.status) }.map { it.id }.sortedByDescending { it }.take(5)
        response.ordersCount shouldBe expectedOrders.size
        response.ordersList.map { it.id } shouldContainInOrder expectedOrders
    }
}
