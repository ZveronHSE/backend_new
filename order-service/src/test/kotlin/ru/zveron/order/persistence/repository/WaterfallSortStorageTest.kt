package ru.zveron.order.persistence.repository

import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import ru.zveron.order.config.BaseOrderApplicationTest
import ru.zveron.order.service.constant.SortBy
import ru.zveron.order.service.constant.SortDirection
import ru.zveron.order.service.model.Sort
import ru.zveron.order.test.util.randomId
import ru.zveron.order.test.util.testOrderLotEntity
import java.time.LocalDate

class WaterfallSortStorageTest @Autowired constructor(
    private val storage: WaterfallStorage,
    private val template: R2dbcEntityTemplate,
) : BaseOrderApplicationTest() {

    @Test
    fun `given correct request, when sort by price asc, then return sorted list of orders`() {
        //prep data
        val sort = Sort(sortBy = SortBy.ByPrice(), sortDirection = SortDirection.ASC)

        //prices from 200 to 2_000
        val orderLotEntities = List(10) { index -> testOrderLotEntity().copy(price = index.inc().times(200).toLong()) }

        //prep env
        runBlocking {
            orderLotEntities.shuffled().forEach { template.insert(it).awaitSingle() }
        }

        //when
        val response = runBlocking {
            storage.findAllPaginated(
                lastId = null,
                pageSize = 10,
                filterParams = emptyList(),
                sort = sort,
            )
        }

        //then
        response.size shouldBe orderLotEntities.size
        response.map { it.id } shouldContainInOrder orderLotEntities.sortedBy { it.price }.map { it.id }
    }

    @Test
    fun `given correct request, when sort by price desc, then return sorted list of orders`() {
        //prep data
        val sort = Sort(sortBy = SortBy.ByPrice(), sortDirection = SortDirection.DESC)

        //prices from 200 to 2_000
        val orderLotEntities = List(10) { index -> testOrderLotEntity().copy(price = index.inc().times(200).toLong()) }

        //prep env
        runBlocking {
            orderLotEntities.shuffled().forEach { template.insert(it).awaitSingle() }
        }

        //when
        val response = runBlocking {
            storage.findAllPaginated(
                lastId = null,
                pageSize = 10,
                filterParams = emptyList(),
                sort = sort,
            )
        }

        //then
        response.size shouldBe orderLotEntities.size
        response.map { it.id } shouldContainInOrder orderLotEntities.sortedByDescending { it.price }.map { it.id }
    }

    @Test
    fun `given correct request, when sort by price and orders have same prices, then sort by id descending`() {
        //prep data
        val sort = Sort(sortBy = SortBy.ByPrice(), sortDirection = SortDirection.ASC)

        //prices from 200 to 2_000
        val orderLotEntities = List(10) { _ -> testOrderLotEntity().copy(price = 1000L) }

        //prep env
        runBlocking {
            orderLotEntities.shuffled().forEach { template.insert(it).awaitSingle() }
        }

        //when
        val response = runBlocking {
            storage.findAllPaginated(
                lastId = null,
                pageSize = 10,
                filterParams = emptyList(),
                sort = sort,
            )
        }

        //then
        response.size shouldBe orderLotEntities.size
        response.map { it.id } shouldContainInOrder orderLotEntities.sortedByDescending { it.id }
            .map { it.id }
    }

    //    @Disabled("works fine in debug and when run solo, but fucks up on test class run")
    @Test
    fun `given correct request, when sort by distance asc then return sorted list of orders`() {
        //prep data

        val orderLotEntities =
            List(10) { index ->
                testOrderLotEntity().copy(
                    id = index.inc().toLong(),
                    subwayId = if (index > 0) index else null
                )
            }

        val sortedIds =
            listOf(0) + orderLotEntities.filter { it.subwayId != null }.map { it.subwayId!! }.shuffled()

        println("initial sort ordering")
        sortedIds.forEach {
            println(it)
        }

        val sort = Sort(sortBy = SortBy.ByDistance(sortedIds = sortedIds), sortDirection = SortDirection.ASC)

        //prep env
        runBlocking {
            orderLotEntities.forEach { template.insert(it).awaitSingle() }
        }

        //when
        val response = runBlocking {
            storage.findAllPaginated(
                lastId = null,
                pageSize = 10,
                filterParams = emptyList(),
                sort = sort,
            )
        }

        //then
        response.size shouldBe 10
        response.first().id shouldBe orderLotEntities.find { it.subwayId == null }!!.id

        println("response")
        response.forEach {
            println("id: ${it.id}, subwayId: ${it.subwayId}")
        }

        println("sortedIds")
        sortedIds.forEach {
            println("id: ${it}")
        }

        response.map { it.subwayId ?: 0 } shouldContainInOrder sortedIds
    }

    @Test
    fun `given correct request, when filter by distance desc, then return filtered list of orders`() {
        //prep data
        val orderLotEntities =
            List(10) { index ->
                testOrderLotEntity().copy(
                    id = index.inc().toLong(),
                    subwayId = if (index > 0) index else null
                )
            }

        val sortedIds =
            listOf(null) + orderLotEntities.filter { it.subwayId != null }.map { it.subwayId!! }.shuffled()
        val sort = Sort(sortBy = SortBy.ByDistance(sortedIds = sortedIds), sortDirection = SortDirection.DESC)

        //prep env
        runBlocking {
            orderLotEntities.shuffled().forEach { template.insert(it).awaitSingle() }
        }

        //when
        val response = runBlocking {
            storage.findAllPaginated(
                lastId = null,
                pageSize = 10,
                filterParams = emptyList(),
                sort = sort,
            )
        }

        //then
        response.size shouldBe 10
        response.last().id shouldBe orderLotEntities.find { it.subwayId == null }!!.id

        response.map { it.subwayId } shouldContainInOrder sortedIds.reversed()
    }

    @Test
    fun `given correct request, when sort by service date desc, then return sorted order list`() {
        //prep data
        val orderLotEntities =
            List(10) { index ->
                testOrderLotEntity().copy(
                    id = randomId(),
                    serviceDateFrom = LocalDate.of(2021, 1, 1).plusDays(index.toLong()),
                    serviceDateTo = LocalDate.of(2021, 1, 1).plusDays(index.toLong() + index.mod(2))
                )
            }

        val sort = Sort(sortBy = SortBy.ByServiceDate(), sortDirection = SortDirection.DESC)
        //prep env
        runBlocking {
            orderLotEntities.shuffled().forEach { template.insert(it).awaitSingle() }
        }

        //when
        val response = runBlocking {
            storage.findAllPaginated(
                lastId = null,
                pageSize = 10,
                filterParams = emptyList(),
                sort = sort,
            )
        }

        //then
        response.size shouldBe 10

        response.map { it.id } shouldContainInOrder orderLotEntities.sortedByDescending { it.serviceDateFrom }
            .map { it.id }
    }

    @Test
    fun `given correct request, when sort by date and some dates intersect, then return sorted list of orders`() {
        //prep data
        val orderLotEntities =
            List(10) { index ->
                testOrderLotEntity().copy(
                    id = index.inc().toLong(),
                    serviceDateFrom = LocalDate.of(2021, 1, 1).plusDays(index.toLong()),
                    serviceDateTo = LocalDate.of(2021, 1, 1).plusDays(index.toLong() + index.mod(2))
                )
            }

        val orderLotOverlaps = List(10) { index ->
            testOrderLotEntity().copy(
                id = 11 + index.toLong(),
                serviceDateFrom = LocalDate.of(2021, 1, 1).plusDays(index.toLong()),
                serviceDateTo = LocalDate.of(2021, 1, 1).plusDays(index.toLong() + index.mod(3))
            )
        }

        val totalOrderLots = orderLotEntities + orderLotOverlaps

        val sort = Sort(sortBy = SortBy.ByServiceDate(), sortDirection = SortDirection.ASC)
        //prep env
        runBlocking {
            totalOrderLots.shuffled().forEach { template.insert(it).awaitSingle() }
        }

        //when
        val response = runBlocking {
            storage.findAllPaginated(
                lastId = null,
                pageSize = 10,
                filterParams = emptyList(),
                sort = sort,
            )
        }

        //then
        response.size shouldBe 10

        response.map { it.id } shouldContainInOrder totalOrderLots.sortedWith { order1, order2 ->
            val dateFromComparison = order1.serviceDateFrom.compareTo(order2.serviceDateFrom)
            if (dateFromComparison == 0) {
                val dateToComparison = order1.serviceDateTo!!.compareTo(order2.serviceDateTo)
                if (dateToComparison == 0) {
                    order2.id!!.compareTo(order1.id!!)
                } else {
                    dateToComparison
                }
            } else {
                dateFromComparison
            }
        }.take(10).map { it.id }
    }
}
