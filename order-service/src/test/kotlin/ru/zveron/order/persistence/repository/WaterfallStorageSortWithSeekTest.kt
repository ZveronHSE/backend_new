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

class WaterfallStorageSortWithSeekTest @Autowired constructor(
        private val storage: WaterfallStorage,
        private val template: R2dbcEntityTemplate,
) : BaseOrderApplicationTest() {

    @Test
    fun `given correct request, when sort by price desc and last id is present, then return sorted list of orders starting from the last id`() {
        //prep data
        val sort = Sort(sortBy = SortBy.ByPrice(), sortDirection = SortDirection.DESC)
        val lastId = 5L

        //prices from 200 to 2_000
        val orderLotEntities = List(10) { index ->
            testOrderLotEntity().copy(
                    id = index.inc().toLong(),
                    price = index.inc().times(200).toLong()
            )
        }

        //prep env
        runBlocking {
            orderLotEntities.shuffled().forEach { template.insert(it).awaitSingle() }
        }

        //when
        val response = runBlocking {
            storage.findAllPaginated(
                    lastId = lastId,
                    pageSize = 10,
                    filters = emptyList(),
                    sort = sort,
            )
        }

        //then
        response.size shouldBe orderLotEntities.size - lastId - 1 //1 account for lastId itself
        response.map { it.id } shouldContainInOrder orderLotEntities.sortedByDescending { it.price }.drop((lastId + 1).toInt()).map { it.id }
    }

    @Test
    fun `given correct request, when filter by distance asc and lastId is null, then return filtered list of orders accounting for the last id being null value`() {
        //prep data
        val orderLotEntities =
                List(10) { index ->
                    testOrderLotEntity().copy(
                            id = index.inc().toLong(),
                            subwayId = if (index > 0) index else null
                    )
                }

        val lastId = 1L

        val sortedIds =
                listOf(null) + orderLotEntities.filter { it.subwayId != null }.map { it.subwayId!! }.shuffled()
        val sort = Sort(sortBy = SortBy.ByDistance(sortedIds = sortedIds), sortDirection = SortDirection.ASC)

        //prep env
        runBlocking {
            orderLotEntities.shuffled().forEach { template.insert(it).awaitSingle() }
        }

        //when
        val response = runBlocking {
            storage.findAllPaginated(
                    lastId = lastId,
                    pageSize = 10,
                    filters = emptyList(),
                    sort = sort,
            )
        }

        //then
        response.size shouldBe 9 // last id not present bc of seek

        response.map { it.subwayId } shouldContainInOrder sortedIds.drop(1) //drop the first one as it's the closest
    }

    @Test
    fun `given correct request, when sort by service date desc and with last id, then return sorted order list accounting for the last id`() {
        //prep data
        val orderLotEntities =
                List(10) { index ->
                    testOrderLotEntity().copy(
                            id = randomId(),
                            serviceDateFrom = LocalDate.of(2021, 1, 1).plusDays(index.toLong()),
                            serviceDateTo = LocalDate.of(2021, 1, 1).plusDays(index.toLong() + index.mod(2))
                    )
                }

        val lastId = orderLotEntities.sortedByDescending { it.serviceDateFrom }.map { it.id }[5]

        val sort = Sort(sortBy = SortBy.ByServiceDate(), sortDirection = SortDirection.DESC)
        //prep env
        runBlocking {
            orderLotEntities.shuffled().forEach { template.insert(it).awaitSingle() }
        }

        //when
        val response = runBlocking {
            storage.findAllPaginated(
                    lastId = lastId,
                    pageSize = 10,
                    filters = emptyList(),
                    sort = sort,
            )
        }

        //then
        response.size shouldBe 4

        response.map { it.id } shouldContainInOrder orderLotEntities.sortedByDescending { it.serviceDateFrom }
                .map { it.id }.dropWhile { it != lastId }.drop(1)
    }
}