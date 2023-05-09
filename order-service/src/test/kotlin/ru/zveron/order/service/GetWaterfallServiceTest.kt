package ru.zveron.order.service

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import ru.zveron.order.client.address.SubwayGrpcClient
import ru.zveron.order.client.address.dto.GetSubwayStationApiResponse
import ru.zveron.order.client.animal.AnimalGrpcClient
import ru.zveron.order.client.animal.dto.GetAnimalApiResponse
import ru.zveron.order.persistence.repository.WaterfallStorage
import ru.zveron.order.service.model.GetWaterfallRequest
import ru.zveron.order.test.util.testGetAnimalResponse
import ru.zveron.order.test.util.testGetSubwayResponse
import ru.zveron.order.test.util.testOrderWrapper

class GetWaterfallServiceTest {

    private val waterfallStorage = mockk<WaterfallStorage>()
    private val animalClient = mockk<AnimalGrpcClient>()
    private val subwayClient = mockk<SubwayGrpcClient>()

    private val service = GetWaterfallService(
        waterfallStorage = waterfallStorage,
        animalGrpcClient = animalClient,
        subwayGrpcClient = subwayClient,
    )

    @Test
    fun `given request to get waterfall without last id, when storage returns empty list, then return empty list`() {
        //prep data
        val request = GetWaterfallRequest(
            pageSize = 5,
        )

        //prep env
        coEvery { waterfallStorage.findAllPaginated(null, request.pageSize, any(), null) } returns emptyList()

        //when
        val response = runBlocking {
            service.getWaterfall(request)
        }

        //then
        response.size shouldBe 0
    }

    @Test
    fun `given request to get waterfall without last id, when storage returns list of 10 elements, then return list of 10 elements`() {
        //prep data
        val request = GetWaterfallRequest(
            pageSize = 10,
        )
        val orderWrapperList = List(10) { testOrderWrapper() }
        val getAnimalResponse = testGetAnimalResponse()
        val getSubwayResponse = testGetSubwayResponse()

        //prep env
        coEvery { waterfallStorage.findAllPaginated(null, request.pageSize, any(), null) } returns orderWrapperList
        coEvery { animalClient.getAnimal(any()) } returns getAnimalResponse
        coEvery { subwayClient.getSubwayStation(any()) } returns getSubwayResponse

        //when
        val response = runBlocking {
            service.getWaterfall(request)
        }

        //then
        response.size shouldBe request.pageSize
    }

    @Test
    fun `given request to get waterfall without last id, when storage returns 10 elements and client cannot find them, then return emptylist`() {
        //prep data
        val pageSize = 10
        val request = GetWaterfallRequest(
            pageSize = pageSize,
        )
        val orderWrapperList = List(10) { testOrderWrapper() }
        val getAnimalResponse = GetAnimalApiResponse.NotFound
        val getSubwayResponse = GetSubwayStationApiResponse.NotFound

        //prep env
        coEvery { waterfallStorage.findAllPaginated(null, pageSize, any(), null) } returns orderWrapperList
        coEvery { animalClient.getAnimal(any()) } returns getAnimalResponse
        coEvery { subwayClient.getSubwayStation(any()) } returns getSubwayResponse

        //when
        val response = runBlocking {
            service.getWaterfall(request)
        }

        //then
        response.size shouldBe 0
    }

    @Test
    fun `given request to get waterfall, when storage returns 2 elements and client cannot find 1, then returns list with 1 element`() {
        //prep data
        val request = GetWaterfallRequest(
            pageSize = 10,
        )
        val orderWrapperList = List(2) { testOrderWrapper() }
        val getAnimalResponseOk = testGetAnimalResponse()
        val getSubwayResponseOk = testGetSubwayResponse()

        //prep env
        coEvery { waterfallStorage.findAllPaginated(null, request.pageSize, any(), null) } returns orderWrapperList
        coEvery { animalClient.getAnimal(any()) } returnsMany listOf(getAnimalResponseOk, GetAnimalApiResponse.NotFound)
        coEvery { subwayClient.getSubwayStation(any()) } returnsMany listOf(
            getSubwayResponseOk,
            GetSubwayStationApiResponse.NotFound
        )

        //when
        val response = runBlocking {
            service.getWaterfall(request)
        }

        //then
        response.size shouldBe 1
    }
}