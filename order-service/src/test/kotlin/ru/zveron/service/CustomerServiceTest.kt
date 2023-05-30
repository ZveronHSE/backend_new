package ru.zveron.service

import io.grpc.Status
import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import ru.zveron.client.address.GetSubwaysApiResponse
import ru.zveron.client.address.SubwayGrpcClient
import ru.zveron.client.animal.AnimalGrpcClient
import ru.zveron.client.animal.GetAnimalsApiResponse
import ru.zveron.client.profile.ProfileGrpcClient
import ru.zveron.client.profile.dto.GetProfileApiResponse
import ru.zveron.exception.ClientException
import ru.zveron.persistence.repository.OrderLotRepository
import ru.zveron.test.util.*

class CustomerServiceTest {

    private val orderLotRepository = mockk<OrderLotRepository>()

    private val profileGrpcClient = mockk<ProfileGrpcClient>()

    private val subwayGrpcClient = mockk<ru.zveron.client.address.SubwayGrpcClient>()

    private val animalGrpcClient = mockk<AnimalGrpcClient>()

    private val service = CustomerService(
        orderLotRepository = orderLotRepository,
        profileGrpcClient = profileGrpcClient,
        subwayGrpcClient = subwayGrpcClient,
        animalGrpcClient = animalGrpcClient,
    )

    @Test
    fun `given correct profile id, when client responds ok and found in db, then process correctly`() {
        //prep data
        val request = randomId()
        val profileIdOrders = List(5) { testOrderLotEntity() }
        val subways = List(5) { index -> testServiceSubwayStation().copy(id = profileIdOrders[index].subwayId!!) }

        val profileResponse = testFindProfileResponse(request)
        val animals = List(5) { index -> testServiceAnimal().copy(id = profileIdOrders[index].animalId) }

        //prep env
        coEvery { orderLotRepository.findAllByProfileId(any()) } returns profileIdOrders
        coEvery { subwayGrpcClient.getSubways(any()) } returns ru.zveron.client.address.GetSubwaysApiResponse.Success(subways)
        coEvery { profileGrpcClient.getProfile(any()) } returns GetProfileApiResponse.Success(profileResponse)
        coEvery { animalGrpcClient.getAnimals(any()) } returns GetAnimalsApiResponse.Success(animals)

        //when
        val response = runBlocking {
            service.getCustomer(request)
        }

        //then
        response.asClue {
            it.profile.asClue { profile ->
                profile.id shouldBe request
                profile.name shouldBe profileResponse.name
                profile.imageUrl shouldBe profileResponse.imageUrl
                profile.rating shouldBe 4.5
            }

            it.orderLots.asClue { orderLots ->
                orderLots.size shouldBe profileIdOrders.size
                orderLots.forEachIndexed { index, orderLot ->
                    orderLot.id shouldBe profileIdOrders[index].id
                    orderLot.title shouldBe profileIdOrders[index].title
                    orderLot.price shouldBe profileIdOrders[index].price
                    orderLot.status shouldBe profileIdOrders[index].status
                    orderLot.serviceDateFrom shouldBe profileIdOrders[index].serviceDateFrom
                    orderLot.serviceDateTo shouldBe profileIdOrders[index].serviceDateTo
                    orderLot.createdAt shouldBe profileIdOrders[index].createdAt
                    orderLot.animal shouldBe animals[index]
                    orderLot.subway shouldBe subways[index]
                }
            }
        }
    }

    @Test
    fun `given request, when nothing is found in database, then return empty list of orders for profile`() {
        //prep data
        val request = randomId()
        val profileResponse = testFindProfileResponse(request)

        //prep env
        coEvery { orderLotRepository.findAllByProfileId(any()) } returns emptyList()
        coEvery { profileGrpcClient.getProfile(any()) } returns GetProfileApiResponse.Success(profileResponse)

        //when
        val response = runBlocking {
            service.getCustomer(request)
        }

        //then
        response.asClue {
            it.profile.asClue { profile ->
                profile.id shouldBe request
                profile.name shouldBe profileResponse.name
                profile.imageUrl shouldBe profileResponse.imageUrl
                profile.rating shouldBe 4.5
            }

            it.orderLots.size shouldBe 0
        }
    }

    @Test
    fun `given request, when orders found in database and client throws exception, then throw exception`() {
        //prep data
        val request = randomId()
        val profileIdOrders = List(5) { testOrderLotEntity() }

        val profileResponse = testFindProfileResponse(request)
        val animals = List(5) { index -> testServiceAnimal().copy(id = profileIdOrders[index].animalId) }

        //prep env
        coEvery { orderLotRepository.findAllByProfileId(any()) } returns profileIdOrders
        coEvery { subwayGrpcClient.getSubways(any()) } returns ru.zveron.client.address.GetSubwaysApiResponse.Error(
            Status.INTERNAL,
            "unknown exception"
        )
        coEvery { profileGrpcClient.getProfile(any()) } returns GetProfileApiResponse.Success(profileResponse)
        coEvery { animalGrpcClient.getAnimals(any()) } returns GetAnimalsApiResponse.Success(animals)

        //when then
        shouldThrowExactly<ClientException> {
            runBlocking {
                service.getCustomer(request)
            }
        }
    }

    @Test
    fun `given correct profile id, when client does not find subway station, then return same amount of orders`() {
        //prep data
        val request = randomId()
        val profileIdOrders = List(5) { testOrderLotEntity() }
        val subways = List(4) { index -> testServiceSubwayStation().copy(id = profileIdOrders[index].subwayId!!) }
        val animals = List(5) { index -> testServiceAnimal().copy(id = profileIdOrders[index].animalId) }

        val subwayResponse = ru.zveron.client.address.GetSubwaysApiResponse.Success(subways)
        val profileResponse = testFindProfileResponse(request)

        //prep env
        coEvery { orderLotRepository.findAllByProfileId(any()) } returns profileIdOrders
        coEvery { subwayGrpcClient.getSubways(any()) } returns subwayResponse
        coEvery { profileGrpcClient.getProfile(any()) } returns GetProfileApiResponse.Success(profileResponse)
        coEvery { animalGrpcClient.getAnimals(any()) } returns GetAnimalsApiResponse.Success(animals)

        //when
        val response = runBlocking {
            service.getCustomer(request)
        }

        //then
        response.asClue {
            it.profile.asClue { profile ->
                profile.id shouldBe request
                profile.name shouldBe profileResponse.name
                profile.imageUrl shouldBe profileResponse.imageUrl
                profile.rating shouldBe 4.5
            }

            it.orderLots.size shouldBe profileIdOrders.size
        }
    }

    @Test
    fun `given correct profile id, when client does not find one of the animals, then return less orders`() {
        //prep data
        val request = randomId()
        val profileIdOrders = List(5) { testOrderLotEntity() }
        val subways = List(5) { index -> testServiceSubwayStation().copy(id = profileIdOrders[index].subwayId!!) }
        val animals = List(4) { index -> testServiceAnimal().copy(id = profileIdOrders[index].animalId) }

        val subwayResponse = ru.zveron.client.address.GetSubwaysApiResponse.Success(subways)
        val profileResponse = testFindProfileResponse(request)

        //prep env
        coEvery { orderLotRepository.findAllByProfileId(any()) } returns profileIdOrders
        coEvery { subwayGrpcClient.getSubways(any()) } returns subwayResponse
        coEvery { profileGrpcClient.getProfile(any()) } returns GetProfileApiResponse.Success(profileResponse)
        coEvery { animalGrpcClient.getAnimals(any()) } returns GetAnimalsApiResponse.Success(animals)

        //when
        val response = runBlocking {
            service.getCustomer(request)
        }

        //then
        response.asClue {
            it.profile.asClue { profile ->
                profile.id shouldBe request
                profile.name shouldBe profileResponse.name
                profile.imageUrl shouldBe profileResponse.imageUrl
                profile.rating shouldBe 4.5
            }

            it.orderLots.size shouldBe animals.size
        }
    }
}
