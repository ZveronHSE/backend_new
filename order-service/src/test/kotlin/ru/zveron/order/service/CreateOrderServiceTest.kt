package ru.zveron.order.service

import io.kotest.assertions.asClue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import ru.zveron.order.component.ClientDecorator
import ru.zveron.order.component.FullOrderExtraData
import ru.zveron.order.exception.ClientException
import ru.zveron.order.persistence.repository.OrderLotRepository
import ru.zveron.order.test.util.*

class CreateOrderServiceTest {
    private val repository = mockk<OrderLotRepository>()
    private val clientDecorator = mockk<ClientDecorator>()

    val service = CreateOrderService(
        orderLotRepository = repository,
        clientDecorator = clientDecorator,
    )

    @Test
    fun `given request to create order, when clients respond ok and subway id is present, then return mapped order`() {
        //prep data
        val request = testCreateOrderRequest()
        val orderExtraDataResponse = FullOrderExtraData(
            profile = testProfile(),
            subwayStation = testServiceSubwayStation(),
            animal = testServiceAnimal(),
        )
        val orderEntity = testOrderLotEntity().copy(
            profileId = orderExtraDataResponse.profile.id,
            animalId = orderExtraDataResponse.animal.id,
            subwayId = orderExtraDataResponse.subwayStation?.id
        )

        //prep env
        coEvery { clientDecorator.getFullOrderData(any(), any(), any()) } returns orderExtraDataResponse
        coEvery { repository.save(any()) } returns orderEntity

        //when
        val result = runBlocking {
            service.createOrder(request)
        }

        //then
        result.shouldNotBeNull().asClue { res ->
            res.profile shouldBe orderExtraDataResponse.profile
            res.animal shouldBe orderExtraDataResponse.animal
            res.subwayStation shouldBe orderExtraDataResponse.subwayStation

            res.id shouldBe orderEntity.id
        }
    }

    @Test
    fun `given request to create order, when clients respond ok and subway id is not present, then create order without subway id`() {
        //prep data
        val request = testCreateOrderRequest()
        val orderExtraDataResponse = FullOrderExtraData(
            profile = testProfile(),
            subwayStation = null,
            animal = testServiceAnimal(),
        )
        val orderEntity = testOrderLotEntity().copy(
            profileId = orderExtraDataResponse.profile.id,
            animalId = orderExtraDataResponse.animal.id,
            subwayId = null
        )

        //prep env
        coEvery { clientDecorator.getFullOrderData(any(), any(), any()) } returns orderExtraDataResponse
        coEvery { repository.save(any()) } returns orderEntity

        //when
        val result = runBlocking {
            service.createOrder(request)
        }

        //then
        result.shouldNotBeNull().asClue { res ->
            res.profile shouldBe orderExtraDataResponse.profile
            res.animal shouldBe orderExtraDataResponse.animal

            res.subwayStation shouldBe null

            res.id shouldBe orderEntity.id
        }
    }

    @Test
    fun `given request to create order, when client decorator throws exception, then throw exception`() {
        //prep data
        val request = testCreateOrderRequest()

        //prep env
        coEvery { clientDecorator.getFullOrderData(any(), any(), any()) } throws ClientException.notFound()

        //when
        val result = runBlocking {
            runCatching {
                service.createOrder(request)
            }
        }

        //then
        result.isFailure shouldBe true
        result.exceptionOrNull() shouldBe ClientException.notFound()
    }
}
