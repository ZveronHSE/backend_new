package ru.zveron.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import ru.zveron.component.ClientDecorator
import ru.zveron.component.FullOrderExtraData
import ru.zveron.exception.ClientException
import ru.zveron.exception.OrderNotFoundException
import ru.zveron.persistence.repository.OrderLotRepository
import ru.zveron.persistence.repository.StatisticsStorage
import ru.zveron.service.mapper.ResponseMapper.mapToFullOrderData
import ru.zveron.test.util.*

class GetOrderServiceTest {

    private val orderLotRepository = mockk<OrderLotRepository>()
    private val clientDecorator = mockk<ClientDecorator>()
    private val statisticsStorage = mockk<StatisticsStorage>(relaxed = true)

    private val service = GetOrderService(
        orderLotRepository = orderLotRepository,
        clientDecorator = clientDecorator,
        statisticsStorage = statisticsStorage,
    )

    @Test
    fun `given correct request, when all clients and repository respond correctly, then return get order response`() {
        //prep data
        val orderId = randomId()

        val orderLotEntity = testOrderLotEntity()
        val profile = testProfile()
        val animal = testServiceAnimal()
        val subway = testServiceSubwayStation()

        val decoratorResponse = FullOrderExtraData(
            profile = profile,
            subwayStation = subway,
            animal = animal,
        )

        //prep env
        coEvery { clientDecorator.getFullOrderData(any(), any(), any()) } returns decoratorResponse
        coEvery { orderLotRepository.findById(any()) } returns orderLotEntity

        //when
        val response = runBlocking {
            service.getOrder(orderId)
        }

        //then
        response shouldBe mapToFullOrderData(orderLotEntity, subway, profile, animal)
    }

    @Test
    fun `given get order request, when order id does not exist, then throw exception`() {
        //prep env
        coEvery { orderLotRepository.findById(any()) } returns null

        //when
        shouldThrow<OrderNotFoundException> {
            runBlocking {
                service.getOrder(randomId())
            }
        }
    }

    @Test
    fun `given get order request, when profile client responds not found, then throw client exception`() {
        //prep data
        val orderId = randomId()
        val orderLotEntity = testOrderLotEntity()

        //prep env
        coEvery { orderLotRepository.findById(any()) } returns orderLotEntity
        coEvery { clientDecorator.getFullOrderData(any(), any(), any()) } throws ClientException.notFound()

        //when
        shouldThrow<ClientException> {
            runBlocking {
                service.getOrder(orderId)
            }
        }
    }
}
