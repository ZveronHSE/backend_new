package ru.zveron.order.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import ru.zveron.order.client.address.SubwayGrpcClient
import ru.zveron.order.client.address.dto.GetSubwayStationApiResponse
import ru.zveron.order.client.animal.AnimalGrpcClient
import ru.zveron.order.client.animal.dto.GetAnimalApiResponse
import ru.zveron.order.client.profile.ProfileGrpcClient
import ru.zveron.order.client.profile.dto.GetProfileApiResponse
import ru.zveron.order.exception.ClientException
import ru.zveron.order.exception.OrderNotFoundException
import ru.zveron.order.mapper.service.mapToGetOrderResponse
import ru.zveron.order.persistence.repository.OrderLotRepository
import ru.zveron.order.service.dto.Animal
import ru.zveron.order.service.dto.Profile
import ru.zveron.order.service.dto.SubwayStation
import ru.zveron.order.util.randomId
import ru.zveron.order.util.testFindProfileResponse
import ru.zveron.order.util.testFullAnimal
import ru.zveron.order.util.testOrderLotEntity
import ru.zveron.order.util.testSubwayStation

class GetOrderServiceTest {


    private val orderLotRepository = mockk<OrderLotRepository>()

    private val profileGrpcClient = mockk<ProfileGrpcClient>()

    private val subwayGrpcClient = mockk<SubwayGrpcClient>()

    private val animalGrpcClient = mockk<AnimalGrpcClient>()

    private val service = GetOrderService(
        orderLotRepository = orderLotRepository,
        profileGrpcClient = profileGrpcClient,
        subwayGrpcClient = subwayGrpcClient,
        animalGrpcClient = animalGrpcClient,
    )


    @Test
    fun `given correct request, when all clients and respoitory respond correctly, then return get order response`() {
        //prep data
        val orderId = randomId()
        val subwayInt = testSubwayStation()
        val subwayResponse = GetSubwayStationApiResponse.Success(subwayInt)
        val profileResponse = testFindProfileResponse()
        val fullAnimal = testFullAnimal()
        val orderLotEntity = testOrderLotEntity()
        val profile = Profile(
            id = profileResponse.id,
            name = profileResponse.name,
            imageUrl = profileResponse.imageUrl,
            rating = 4.5
        )
        val animal = Animal(
            id = fullAnimal.id,
            name = fullAnimal.name,
            species = fullAnimal.species,
            breed = fullAnimal.breed,
            imageUrl = fullAnimal.imageUrlsList.first()
        )
        val subway = SubwayStation(
            name = subwayInt.name,
            colorHex = subwayInt.colorHex,
            town = subwayInt.town
        )

        //prep env
        coEvery { subwayGrpcClient.getSubwayStation(any()) } returns subwayResponse
        coEvery { profileGrpcClient.getProfile(any()) } returns GetProfileApiResponse.Success(profileResponse)
        coEvery { animalGrpcClient.getAnimal(any()) } returns GetAnimalApiResponse.Success(fullAnimal)
        coEvery { orderLotRepository.findById(any()) } returns orderLotEntity

        //when
        val response = runBlocking {
            service.getOrder(orderId)
        }

        //then
        response shouldBe mapToGetOrderResponse(orderLotEntity, subway, profile, animal)
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
        val subwayInt = testSubwayStation()
        val profileResponse = testFindProfileResponse()
        val subwayResponse = GetSubwayStationApiResponse.Success(subwayInt)
        val orderLotEntity = testOrderLotEntity()

        //prep env
        coEvery { subwayGrpcClient.getSubwayStation(any()) } returns subwayResponse
        coEvery { profileGrpcClient.getProfile(any()) } returns GetProfileApiResponse.Success(profileResponse)
        coEvery { animalGrpcClient.getAnimal(any()) } returns GetAnimalApiResponse.NotFound
        coEvery { orderLotRepository.findById(any()) } returns orderLotEntity

        //when
        shouldThrow<ClientException> {
            runBlocking {
                service.getOrder(orderId)
            }
        }
    }
}
