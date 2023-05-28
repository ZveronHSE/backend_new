package ru.zveron.persistence.repository

import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import ru.zveron.config.BaseOrderApplicationTest
import ru.zveron.persistence.model.constant.Status
import ru.zveron.service.constant.Field
import ru.zveron.service.constant.Operation
import ru.zveron.service.model.FilterParam
import ru.zveron.test.util.LocalDateUtil.isAfterOrEqual
import ru.zveron.test.util.LocalDateUtil.isBeforeOrEqual
import ru.zveron.test.util.shouldBeAfterOrEqual
import ru.zveron.test.util.shouldBeBeforeOrEqual
import ru.zveron.test.util.testOrderLotEntity
import java.time.LocalDate

class WaterfallFilterStorageTest @Autowired constructor(
    private val template: R2dbcEntityTemplate,
    private val storage: WaterfallStorage,
) : BaseOrderApplicationTest() {

    @Test
    fun `given correct request, when filter by price less than, then return filtered list of order`() {
        //prep data
        val filtersParams = listOf(FilterParam(field = Field.PRICE, operation = Operation.LESS_THAN, value = "1000"))

        //prices from 200 to 2_000
        val orderLotEntities = List(10) { index -> testOrderLotEntity().copy(price = index.inc().times(200).toLong()) }

        //prep env
        runBlocking {
            orderLotEntities.forEach { template.insert(it).awaitSingle() }
        }

        //when
        val response = runBlocking {
            storage.findAllPaginated(
                lastId = null,
                pageSize = 10,
                filterParams = filtersParams,
            )
        }

        //then
        response.size shouldBeGreaterThan 0
        response.size shouldBe orderLotEntities.filter { it.price < 1000 }.size
        response.forEach { it.price shouldBeLessThan 1000L }
    }

    @Test
    fun `given correct request, whehn filter by price greater than, then return filtered list of orders`() {
        //prep data
        val filtersParams = listOf(FilterParam(field = Field.PRICE, operation = Operation.GREATER_THAN, value = "1000"))

        //prices from 200 to 2_000
        val orderLotEntities = List(10) { index -> testOrderLotEntity().copy(price = index.inc().times(200).toLong()) }

        //prep env
        runBlocking {
            orderLotEntities.forEach { template.insert(it).awaitSingle() }
        }

        //when
        val response = runBlocking {
            storage.findAllPaginated(
                lastId = null,
                pageSize = 10,
                filterParams = filtersParams,
            )
        }

        //then
        response.size shouldBeGreaterThan 0
        response.size shouldBe orderLotEntities.filter { it.price > 1000 }.size
        response.forEach { it.price shouldBeGreaterThan 1000L }
    }

    @Test
    fun `given correct request, when filter by price greater than and less than, then return filtered list of orders`() {
        //prep data
        val filtersParams = listOf(
            FilterParam(field = Field.PRICE, operation = Operation.GREATER_THAN, value = "1000"),
            FilterParam(field = Field.PRICE, operation = Operation.LESS_THAN, value = "1500"),
        )

        //prices from 200 to 2_000
        val orderLotEntities = List(10) { index -> testOrderLotEntity().copy(price = index.inc().times(200).toLong()) }

        //prep env
        runBlocking {
            orderLotEntities.forEach { template.insert(it).awaitSingle() }
        }

        //when
        val response = runBlocking {
            storage.findAllPaginated(
                lastId = null,
                pageSize = 10,
                filterParams = filtersParams,
            )
        }

        //then
        response.size shouldBeGreaterThan 0
        response.size shouldBe orderLotEntities.filter { it.price in 1001..1499 }.size
        response.forEach { it.price shouldBeGreaterThan 1000L }
        response.forEach { it.price shouldBeLessThan 1500L }
    }

    @Test
    fun `given correct request, when filter by service date from and service date to, then return filtered list`() {
        //prep data
        val filtersParams = listOf(
            FilterParam(
                field = Field.SERVICE_DATE_FROM,
                operation = Operation.GREATER_THAN_EQUALITY,
                value = "2021-01-03"
            ),
            FilterParam(field = Field.SERVICE_DATE_TO, operation = Operation.LESS_THAN_EQUALITY, value = "2021-01-05"),
        )

        //prep env
        val orderLotEntities = List(10) { index ->
            testOrderLotEntity()
                .copy(
                    serviceDateFrom = LocalDate.of(2021, 1, index.inc()),
                    serviceDateTo = if (index.mod(2) == 1) LocalDate.of(2021, 1, index.plus(2)) else LocalDate.of(
                        2021,
                        1,
                        index.inc()
                    ),
                )
        }

        //prep env
        runBlocking {
            orderLotEntities.forEach { template.insert(it).awaitSingle() }
        }

        //when
        val response = runBlocking {
            storage.findAllPaginated(
                lastId = null,
                pageSize = 10,
                filterParams = filtersParams,
            )
        }

        //then
        val afterDate = LocalDate.of(2021, 1, 3)
        val beforeDate = LocalDate.of(2021, 1, 5)
        response.size shouldBe orderLotEntities.filter {
            it.serviceDateFrom.isAfterOrEqual(afterDate)
                    && it.serviceDateTo!!.isBeforeOrEqual(beforeDate)
        }.size

        response.forEach { it.serviceDateFrom shouldBeAfterOrEqual LocalDate.of(2021, 1, 3) }
        response.forEach { it.serviceDateTo!! shouldBeBeforeOrEqual LocalDate.of(2021, 1, 5) }
    }

    @Test
    fun `given correct request, when filter by service type, then return filtered list`() {
        //prep data
        val serviceTypes = "WALK,SITTING,BOARDING"
        val filtersParams = listOf(
            FilterParam(field = Field.SERVICE_TYPE, operation = Operation.IN, value = serviceTypes),
        )

        //prep env
        val orderLotEntities = List(10) { testOrderLotEntity() }

        //prep env
        runBlocking {
            orderLotEntities.forEach { template.insert(it).awaitSingle() }
        }

        //when
        val response = runBlocking {
            storage.findAllPaginated(
                lastId = null,
                pageSize = 10,
                filterParams = filtersParams,
            )
        }

        //then
        response.size shouldBeGreaterThan 0
        response.size shouldBe orderLotEntities.filter { it.serviceType.name in serviceTypes.split(",") }.size
        response.forEach { it.serviceType.name shouldBeIn serviceTypes.split(",") }
    }

    @Test
    fun `given correct request, when filter by service delivery type, then return filtered list`() {
        //prep data
        val serviceDeliveryTypes = "IN_PERSON"
        val filtersParams = listOf(
            FilterParam(field = Field.SERVICE_DELIVERY_TYPE, operation = Operation.IN, value = serviceDeliveryTypes),
        )

        //prep env
        val orderLotEntities = List(10) { testOrderLotEntity() }

        //prep env
        runBlocking {
            orderLotEntities.forEach { template.insert(it).awaitSingle() }
        }

        //when
        val response = runBlocking {
            storage.findAllPaginated(
                lastId = null,
                pageSize = 10,
                filterParams = filtersParams,
            )
        }

        //then
        response.size shouldBeGreaterThan 0
        response.size shouldBe orderLotEntities.filter { it.serviceDeliveryType.name in serviceDeliveryTypes.split(",") }.size
        response.forEach { it.serviceDeliveryType.name shouldBeIn serviceDeliveryTypes.split(",") }
    }

    @Test
    fun `given correct request, when filter by status, then return filtered list of orders`() {
        //prep data
        val statuses = "CANCELLED,COMPLETED"
        val filtersParams = listOf(
            FilterParam(field = Field.STATUS, operation = Operation.NOT_IN, value = statuses),
        )

        //prep env
        val terminalOrderEntities = List(10) {
            testOrderLotEntity().copy(
                status = if (it.mod(2) == 1) Status.CANCELLED else Status.COMPLETED
            )
        }
        val orderLotEntities = terminalOrderEntities + listOf(
            testOrderLotEntity().copy(status = Status.PENDING),
            testOrderLotEntity().copy(status = Status.UPDATING)
        )

        //prep env
        runBlocking {
            orderLotEntities.forEach { template.insert(it).awaitSingle() }
        }

        //when
        val response = runBlocking {
            storage.findAllPaginated(
                lastId = null,
                pageSize = 10,
                filterParams = filtersParams,
            )
        }

        //then
        response.size shouldBe 2
    }
}