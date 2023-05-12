package ru.zveron.order.entrpoint

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.awaitFirst
import ru.zveron.contract.order.external.copy
import ru.zveron.contract.order.external.getOrderRequest
import ru.zveron.order.client.address.dto.GetSubwayStationApiResponse
import ru.zveron.order.client.animal.dto.GetAnimalApiResponse
import ru.zveron.order.client.profile.dto.GetProfileApiResponse
import ru.zveron.order.config.BaseOrderApplicationTest
import ru.zveron.order.exception.ClientException
import ru.zveron.order.persistence.entity.OrderLot
import ru.zveron.order.test.util.*
import ru.zveron.order.util.PriceFormatter

class OrderServiceEntrypointTest @Autowired constructor(
    private val entrypoint: OrderServiceEntrypoint,
    private val template: R2dbcEntityTemplate,
) : BaseOrderApplicationTest() {

    @Test
    fun `given correct request, when clients respond and database has order, then return order response`() {
        //prep data
        val orderId = randomId()
        val request = getOrderRequest { this.id = orderId }
        val subway = testSubwayStation()
        val profileResponse = testFindProfileResponse()
        val animal = testFullAnimal()
        val orderLotEntity = testOrderLotEntity().copy(id = orderId)
        val subwayResponse = GetSubwayStationApiResponse.Success(subway)

        //prep env
        runBlocking {
            template.insert(orderLotEntity).awaitSingle()
        }

        coEvery { subwayGrpcClient.getSubwayStation(any()) } returns subwayResponse
        coEvery { profileGrpcClient.getProfile(any()) } returns GetProfileApiResponse.Success(profileResponse)
        coEvery { animalGrpcClient.getAnimal(any()) } returns GetAnimalApiResponse.Success(animal)


        //when
        val response = runBlocking {
            entrypoint.getOrder(request)
        }

        //then
        response.order.shouldNotBeNull().asClue {
            it.id shouldBe orderId
            it.profile.id shouldBe profileResponse.id
            it.animal.id shouldBe animal.id
            it.address.station shouldBe subway.name
            it.price shouldBe """${orderLotEntity.price} ₽"""
            it.title shouldBe orderLotEntity.title
            it.description shouldBe orderLotEntity.description
        }
    }

    @Test
    fun `given correct request, when database has order, but clients respond with error, then throws exception`() {
        //prep data
        val orderId = randomId()
        val request = getOrderRequest { this.id = orderId }
        val subway = testSubwayStation()
        val profileResponse = testFindProfileResponse()

        val orderLotEntity = testOrderLotEntity().copy(id = orderId)

        val subwayResponse = GetSubwayStationApiResponse.Success(subway)

        //prep env
        runBlocking {
            template.insert(orderLotEntity).awaitSingle()
        }

        coEvery { subwayGrpcClient.getSubwayStation(any()) } returns subwayResponse
        coEvery { profileGrpcClient.getProfile(any()) } returns GetProfileApiResponse.Success(profileResponse)
        coEvery { animalGrpcClient.getAnimal(any()) } returns GetAnimalApiResponse.NotFound


        //when
        shouldThrowExactly<ClientException> {
            runBlocking {
                entrypoint.getOrder(request)
            }
        }
    }

    @Test
    fun `smoke test for create order`() {
        //prep data
        val subway = testSubwayStation()
        val profileResponse = testFindProfileResponse()
        val animal = testFullAnimal()

        val request = testCreateOrderEntrypointRequest().copy {
            subwayStationId = subway.id.toLong()
            profileId = profileResponse.id
            animalId = animal.id
        }

        //prep env
        coEvery { subwayGrpcClient.getSubwayStation(any()) } returns GetSubwayStationApiResponse.Success(subway)
        coEvery { profileGrpcClient.getProfile(any()) } returns GetProfileApiResponse.Success(profileResponse)
        coEvery { animalGrpcClient.getAnimal(any()) } returns GetAnimalApiResponse.Success(animal)

        //when
        val response = runBlocking {
            entrypoint.createOrder(request)
        }

        //then
        val entity = runBlocking {
            template.select(OrderLot::class.java).awaitFirst()
        }

        response.fullOrder.asClue {
            it.id shouldBe entity.id
            it.profile.id shouldBe profileResponse.id
            it.animal.id shouldBe animal.id
            it.address.station shouldBe subway.name
            it.price shouldBe PriceFormatter.formatToPrice(entity.price)
            it.title shouldBe entity.title
            it.description shouldBe entity.description
        }
    }
}
