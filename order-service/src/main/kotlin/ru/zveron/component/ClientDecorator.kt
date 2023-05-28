package ru.zveron.component

import io.grpc.Status
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import org.springframework.stereotype.Component
import ru.zveron.client.address.SubwayGrpcClient
import ru.zveron.client.address.dto.GetSubwayStationApiResponse
import ru.zveron.client.animal.AnimalGrpcClient
import ru.zveron.client.animal.GetAnimalsApiResponse
import ru.zveron.client.animal.dto.GetAnimalApiResponse
import ru.zveron.client.profile.ProfileGrpcClient
import ru.zveron.client.profile.dto.GetProfileApiResponse
import ru.zveron.exception.ClientException
import ru.zveron.service.mapper.ModelMapper.of
import ru.zveron.service.model.Animal
import ru.zveron.service.model.Profile
import ru.zveron.service.model.SubwayStation

@Component
class ClientDecorator(
    private val profileGrpcClient: ProfileGrpcClient,
    private val subwayGrpcClient: ru.zveron.client.address.SubwayGrpcClient,
    private val animalGrpcClient: AnimalGrpcClient,
) {

    suspend fun getFullOrderData(profileId: Long, animalId: Long, subwayId: Int?) = supervisorScope {
        val rating = async { getRating(profileId) }
        val profile = async { getProfile(profileId, rating.await()) }
        val subwayStation = async { getSubwayStation(subwayId) }
        val animal = async { getAnimal(animalId) }

        return@supervisorScope FullOrderExtraData(profile.await(), subwayStation.await(), animal.await())
    }

    private suspend fun getAnimals(ids: List<Long>): List<Animal> {
        when (val response = animalGrpcClient.getAnimals(ids)) {
            is GetAnimalsApiResponse.Error -> throw ClientException(
                message = "Get animals client request failed",
                status = response.error
            )

            is GetAnimalsApiResponse.Success -> return response.animals
        }
    }

    //todo: request views and likes
    suspend fun getAnimalsData(animalIds: List<Long>) = getAnimals(animalIds)

    private suspend fun getProfile(profileId: Long, rating: Double): Profile =
        when (val response = profileGrpcClient.getProfile(profileId)) {
            is GetProfileApiResponse.Error -> throw ClientException(
                message = "Get profile client request failed",
                status = response.error
            )

            GetProfileApiResponse.NotFound -> throw ClientException(
                message = "Profile not found",
                status = Status.NOT_FOUND
            )

            is GetProfileApiResponse.Success -> Profile.of(response.profile, rating)
        }

    private suspend fun getRating(profileId: Long): Double = 4.5

    private suspend fun getSubwayStation(subwayId: Int?): SubwayStation? {
        if (subwayId == null) return null
        return when (val response = subwayGrpcClient.getSubwayStation(subwayId)) {
            is GetSubwayStationApiResponse.Error -> throw ClientException(
                message = "Get subway station client request failed",
                status = response.error
            )

            GetSubwayStationApiResponse.NotFound -> throw ClientException(
                message = "Subway station not found",
                status = Status.NOT_FOUND
            )

            is GetSubwayStationApiResponse.Success -> SubwayStation.of(response.subwayStation)
        }
    }

    private suspend fun getAnimal(animalId: Long): Animal =
        when (val response = animalGrpcClient.getAnimal(animalId)) {
            is GetAnimalApiResponse.Error -> throw ClientException(
                message = "Get animal client request failed",
                status = response.error
            )

            GetAnimalApiResponse.NotFound -> throw ClientException(
                message = "Animal not found",
                status = Status.NOT_FOUND
            )

            is GetAnimalApiResponse.Success -> Animal.of(response.animal)
        }
}
