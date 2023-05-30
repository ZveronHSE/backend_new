package ru.zveron.component

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import ru.zveron.client.address.SubwayGrpcClient
import ru.zveron.client.address.dto.GetSubwayStationApiResponse
import ru.zveron.client.animal.AnimalGrpcClient
import ru.zveron.client.animal.dto.GetAnimalApiResponse
import ru.zveron.client.profile.ProfileGrpcClient
import ru.zveron.client.profile.dto.GetProfileApiResponse
import ru.zveron.exception.ClientException
import ru.zveron.service.model.Animal
import ru.zveron.service.model.Profile
import ru.zveron.service.model.SubwayStation
import ru.zveron.test.util.testFindProfileResponse
import ru.zveron.test.util.testFullAnimal
import ru.zveron.test.util.testOrderLotEntity
import ru.zveron.test.util.testSubwayStation

class ClientDecoratorTest {
    private val profileGrpcClient = mockk<ProfileGrpcClient>()
    private val subwayGrpcClient = mockk<ru.zveron.client.address.SubwayGrpcClient>()
    private val animalGrpcClient = mockk<AnimalGrpcClient>()

    private val decorator = ClientDecorator(
        profileGrpcClient = profileGrpcClient,
        subwayGrpcClient = subwayGrpcClient,
        animalGrpcClient = animalGrpcClient,
    )

    @Test
    fun `given correct request, when all clients respond correctly, then return order extra data`() {
        //prep data
        val subwayInternal = testSubwayStation()
        val profileResponse = testFindProfileResponse()
        val fullAnimal = testFullAnimal()
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
            id = subwayInternal.id,
            name = subwayInternal.name,
            colorHex = subwayInternal.colorHex,
            town = subwayInternal.town
        )

        //prep env
        coEvery { subwayGrpcClient.getSubwayStation(any()) } returns GetSubwayStationApiResponse.Success(subwayInternal)
        coEvery { profileGrpcClient.getProfile(any()) } returns GetProfileApiResponse.Success(profileResponse)
        coEvery { animalGrpcClient.getAnimal(any()) } returns GetAnimalApiResponse.Success(fullAnimal)

        //when
        val result = runBlocking {
            decorator.getFullOrderData(profileId = profile.id, animalId = animal.id, subwayId = subway.id)
        }

        //then
        result.asClue {
            it.profile shouldBe profile
            it.animal shouldBe animal
            it.subwayStation shouldBe subway
        }
    }

    @Test
    fun `given request to find extra data of an order, when one of the clients cannot find the entity, then throw client exception`() {
        //prep data
        val subwayInternal = testSubwayStation()
        val profileResponse = testFindProfileResponse()
        val orderLotEntity = testOrderLotEntity()

        //prep env
        coEvery { subwayGrpcClient.getSubwayStation(any()) } returns GetSubwayStationApiResponse.Success(subwayInternal)
        coEvery { profileGrpcClient.getProfile(any()) } returns GetProfileApiResponse.Success(profileResponse)
        coEvery { animalGrpcClient.getAnimal(any()) } returns GetAnimalApiResponse.NotFound

        //when
        shouldThrowExactly<ClientException> {
            runBlocking {
                decorator.getFullOrderData(
                    profileId = orderLotEntity.profileId,
                    animalId = orderLotEntity.animalId,
                    subwayId = orderLotEntity.subwayId
                )
            }
        }
    }
}
