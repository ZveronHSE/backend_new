package ru.zveron.order.persistence.repository

import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import ru.zveron.order.config.BaseOrderApplicationTest
import ru.zveron.order.test.util.shouldBeOrderLot
import ru.zveron.order.test.util.testOrderLotEntity

class WaterfallStorageTest @Autowired constructor(
    private val template: R2dbcEntityTemplate,
    private val waterfallStorage: WaterfallStorage,
) : BaseOrderApplicationTest() {

    @Test
    fun `given request to get unsorted, unfiltered orders without last order id, then return list of orders by id desc`() {
        //prep data
        val orderLotEntities = List(10) { testOrderLotEntity() }
        val pageSize = 8

        //prep env
        val orderLotIds = runBlocking {
            orderLotEntities.map { template.insert(it).awaitSingle().id }
        }

        //when
        val response = runBlocking {
            waterfallStorage.findAllPaginated(
                lastId = null,
                pageSize = pageSize,
            )
        }

        //then
        response.size shouldBe pageSize
        response.map { it.id } shouldContainInOrder orderLotIds.sortedByDescending { it }.take(pageSize)
    }

    @Test
    fun `given request to get unsorted, unfiltered orders, without last order id, when db contains less than pagination, then return what db contains`() {
        //prep data
        val orderLotEntities = List(5) { testOrderLotEntity() }
        val pageSize = 8

        //prep env
        val orderLotIds = runBlocking {
            orderLotEntities.map { template.insert(it).awaitSingle().id }
        }

        //when
        val response = runBlocking {
            waterfallStorage.findAllPaginated(
                lastId = null,
                pageSize = pageSize,
            )
        }

        //then
        response.size shouldBe orderLotEntities.size
        response.forEach {
            val orderEntity = orderLotEntities.find { entity -> it.id == entity.id }!!
            it.asClue { wrap ->
                wrap shouldBeOrderLot orderEntity
            }
        }

        response.map { it.id } shouldContainInOrder orderLotIds.sortedByDescending { it }
    }

    @Test
    fun `given request to get unsorted, unfiltered orders, with last order id, then return all orders with higher ids`() {
        //prep data
        val orderLotEntities = List(10) { index -> testOrderLotEntity().copy(id = index.inc().toLong()) }
        val pageSize = 8
        val lastOrderId = 5L

        //prep env
        val orderLotIds = runBlocking {
            orderLotEntities.map { template.insert(it).awaitSingle().id }
        }

        //when
        val response = runBlocking {
            waterfallStorage.findAllPaginated(
                lastId = lastOrderId,
                pageSize = pageSize,
            )
        }

        //then
        val expectedSize = pageSize - lastOrderId + 1 // +1 because array indexing starts with 0
        response.size shouldBe expectedSize
        response.forEach {
            val orderEntity = orderLotEntities.find { entity -> it.id == entity.id }!!
            it.asClue { wrap ->
                wrap shouldBeOrderLot orderEntity
            }
        }

        println(orderLotIds.sortedByDescending { it })
        println(response.map { it.id })
        println(orderLotIds.sortedByDescending { it }
            .dropWhile { it != lastOrderId }.take(lastOrderId.toInt()))

        response.map { it.id } shouldContainInOrder orderLotIds.sortedByDescending { it }
            .dropWhile { it!! >= lastOrderId }.take(expectedSize.toInt())
    }
}
