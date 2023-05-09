package ru.zveron.order.service.mapper

import io.kotest.assertions.asClue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import ru.zveron.order.test.util.testOrderWrapper
import ru.zveron.order.test.util.testServiceAnimal
import ru.zveron.order.test.util.testServiceSubwayStation

class ResponseMapperTest {

    @Test
    fun `given order lot records, subway stations and animals, when data is correct, then return correct mapping`() {
        //prep data
        val orderLot = testOrderWrapper().copy(id = 1L, subwayId = 1, animalId = 1)
        val orderLotRecords = listOf(orderLot)
        val subwayStation = testServiceSubwayStation()
        val subwayStations = mapOf(1 to subwayStation)
        val animal = testServiceAnimal().copy(id = 1)
        val animals = mapOf(1L to animal)

        //when
        val response = ResponseMapper.toGetOrderWaterfallResponse(orderLotRecords, subwayStations, animals)

        //then
        val singleResponse = response[0]

        singleResponse.shouldNotBeNull().asClue {
            it.id shouldBe orderLot.id
            it.subway.asClue { s ->
                s.name shouldBe subwayStation.name
                s.colorHex shouldBe subwayStation.colorHex
                s.town shouldBe subwayStation.town
            }
            it.price shouldBe orderLot.price
            it.createdAt shouldBe orderLot.createdAt
            it.serviceDateFrom shouldBe orderLot.serviceDateFrom
            it.serviceDateTo shouldBe orderLot.serviceDateTo
            it.title shouldBe orderLot.title
            it.animal.asClue { a ->
                a.id shouldBe animal.id
                a.name shouldBe animal.name
                a.breed shouldBe animal.breed
                a.species shouldBe animal.species
                a.imageUrl shouldBe animal.imageUrl
            }
        }
    }

    @Test
    fun `given a null animal in map, then returns emptylist`(){
        //prep data
        val orderLot = testOrderWrapper().copy(id = 1L, subwayId = 1, animalId = 1)
        val orderLotRecords = listOf(orderLot)
        val subwayStation = testServiceSubwayStation()
        val subwayStations = mapOf(1 to subwayStation)
        val animal = testServiceAnimal().copy(id = 1)
        val animals = mapOf(2L to animal)

        //when
        val response = ResponseMapper.toGetOrderWaterfallResponse(orderLotRecords, subwayStations, animals)

        //then
        response.size shouldBe 0
    }

    @Test
    fun `given a null station in map, then returns emptylist`(){
        //prep data
        val orderLot = testOrderWrapper().copy(id = 1L, subwayId = 1, animalId = 1)
        val orderLotRecords = listOf(orderLot)
        val subwayStation = testServiceSubwayStation()
        val subwayStations = mapOf(2 to subwayStation)
        val animal = testServiceAnimal().copy(id = 1)
        val animals = mapOf(1L to animal)

        //when
        val response = ResponseMapper.toGetOrderWaterfallResponse(orderLotRecords, subwayStations, animals)

        //then
        response.size shouldBe 0
    }
}