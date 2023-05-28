package ru.zveron.entrpoint

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
import ru.zveron.library.grpc.interceptor.model.MetadataElement
import ru.zveron.library.grpc.model.Metadata
import ru.zveron.client.address.dto.GetSubwayStationApiResponse
import ru.zveron.client.animal.GetAnimalsApiResponse
import ru.zveron.client.animal.dto.GetAnimalApiResponse
import ru.zveron.client.profile.dto.GetProfileApiResponse
import ru.zveron.config.BaseOrderApplicationTest
import ru.zveron.exception.ClientException
import ru.zveron.persistence.entity.OrderLot
import ru.zveron.test.util.randomId
import ru.zveron.test.util.testCreateOrderEntrypointRequest
import ru.zveron.test.util.testFindProfileResponse
import ru.zveron.test.util.testFullAnimal
import ru.zveron.test.util.testOrderLotEntity
import ru.zveron.test.util.testServiceAnimal
import ru.zveron.test.util.testSubwayStation
import ru.zveron.util.PriceFormatter

class OrderServiceEntrypointTest @Autowired constructor(
    private val entrypoint: OrderServiceEntrypoint,
    private val template: R2dbcEntityTemplate,
) : BaseOrderApplicationTest() {

    @Test
    fun `given correct request, when clients respond and database has order, then return order response`() {
        // prep data
        val orderId = randomId()
        val request = getOrderRequest { this.id = orderId }
        val subway = testSubwayStation()
        val profileResponse = testFindProfileResponse()
        val animal = testFullAnimal()
        val orderLotEntity = testOrderLotEntity().copy(id = orderId)
        val subwayResponse = GetSubwayStationApiResponse.Success(subway)

        // prep env
        runBlocking {
            template.insert(orderLotEntity).awaitSingle()
        }

        coEvery { subwayGrpcClient.getSubwayStation(any()) } returns subwayResponse
        coEvery { profileGrpcClient.getProfile(any()) } returns GetProfileApiResponse.Success(profileResponse)
        coEvery { animalGrpcClient.getAnimal(any()) } returns GetAnimalApiResponse.Success(animal)

        // when
        val response = runBlocking {
            entrypoint.getOrder(request)
        }

        // then
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
        // prep data
        val orderId = randomId()
        val request = getOrderRequest { this.id = orderId }
        val subway = testSubwayStation()
        val profileResponse = testFindProfileResponse()

        val orderLotEntity = testOrderLotEntity().copy(id = orderId)

        val subwayResponse = GetSubwayStationApiResponse.Success(subway)

        // prep env
        runBlocking {
            template.insert(orderLotEntity).awaitSingle()
        }

        coEvery { subwayGrpcClient.getSubwayStation(any()) } returns subwayResponse
        coEvery { profileGrpcClient.getProfile(any()) } returns GetProfileApiResponse.Success(profileResponse)
        coEvery { animalGrpcClient.getAnimal(any()) } returns GetAnimalApiResponse.NotFound

        // when
        shouldThrowExactly<ClientException> {
            runBlocking {
                entrypoint.getOrder(request)
            }
        }
    }

    @Test
    fun `smoke test for create order`() {
        // prep data
        val subway = testSubwayStation()
        val profileResponse = testFindProfileResponse()
        val animal = testFullAnimal()

        val request = testCreateOrderEntrypointRequest().copy {
            subwayStationId = subway.id
            animalId = animal.id
        }

        // prep env
        coEvery { subwayGrpcClient.getSubwayStation(any()) } returns GetSubwayStationApiResponse.Success(subway)
        coEvery { profileGrpcClient.getProfile(any()) } returns GetProfileApiResponse.Success(profileResponse)
        coEvery { animalGrpcClient.getAnimal(any()) } returns GetAnimalApiResponse.Success(animal)

        // when
        val response = runBlocking(MetadataElement(Metadata(randomId()))) {
            entrypoint.createOrder(request)
        }

        // then
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

    @Test
    fun `smoke test for get order lots by profile id`() {
        // prep data
        val orderId = randomId()
        val request = getOrderRequest { this.id = orderId }
        val subway = testSubwayStation()
        val profileResponse = testFindProfileResponse()
        val animal = testFullAnimal()
        val orderLotEntity = testOrderLotEntity().copy(id = orderId)
        val subwayResponse = GetSubwayStationApiResponse.Success(subway)

        // prep env
        runBlocking {
            template.insert(orderLotEntity).awaitSingle()
        }

        coEvery { subwayGrpcClient.getSubwayStation(any()) } returns subwayResponse
        coEvery { profileGrpcClient.getProfile(any()) } returns GetProfileApiResponse.Success(profileResponse)
        coEvery { animalGrpcClient.getAnimal(any()) } returns GetAnimalApiResponse.Success(animal)

        // when
        val response = runBlocking {
            entrypoint.getOrder(request)
        }

        // then
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
    fun `smoke test for get orders for profile`() {
        // prep data
        val profileId = randomId()
        val orderLots = List(5) { testOrderLotEntity().copy(profileId = profileId) }
        val testAnimals = orderLots.map { testServiceAnimal().copy(id = it.animalId) }

        // prep env
        runBlocking {
            orderLots.forEach { template.insert(it).awaitSingle() }
        }

        coEvery { animalGrpcClient.getAnimals(any()) } returns GetAnimalsApiResponse.Success(testAnimals)

//        //when
//        val response = runBlocking(MetadataElement(Metadata(profileId))) {
//            entrypoint.getOrdersByProfile(empty { })
//        }
//
//        //then
//        response.shouldNotBeNull().asClue {
//            it.ordersCount shouldBe 5
//            it.ordersList.map { order -> order.id } shouldContainExactlyInAnyOrder orderLots.map { order -> order.id }
//        }
    }
}
