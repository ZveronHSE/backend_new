package ru.zveron.order.service

import io.grpc.Status
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import org.springframework.stereotype.Service
import ru.zveron.order.client.address.SubwayGrpcClient
import ru.zveron.order.client.address.dto.GetSubwayStationApiResponse
import ru.zveron.order.client.animal.AnimalGrpcClient
import ru.zveron.order.client.animal.dto.GetAnimalApiResponse
import ru.zveron.order.client.profile.ProfileGrpcClient
import ru.zveron.order.client.profile.dto.GetProfileApiResponse
import ru.zveron.order.exception.ClientException
import ru.zveron.order.exception.OrderNotFoundException
import ru.zveron.order.mapper.service.mapToGetOrderResponse
import ru.zveron.order.mapper.service.of
import ru.zveron.order.persistence.repository.OrderLotRepository
import ru.zveron.order.service.dto.Animal
import ru.zveron.order.service.dto.GetOrderResponse
import ru.zveron.order.service.dto.Profile
import ru.zveron.order.service.dto.SubwayStation

@Service
class GetOrderService(
    private val orderLotRepository: OrderLotRepository,
    private val profileGrpcClient: ProfileGrpcClient,
    private val subwayGrpcClient: SubwayGrpcClient,
    private val animalGrpcClient: AnimalGrpcClient,
) {

    suspend fun getOrder(orderId: Long): GetOrderResponse = supervisorScope {
        val order = orderLotRepository.findById(orderId) ?: throw OrderNotFoundException(orderId)
        val rating = async { getRating(order.profileId) }
        val profile = async { getProfile(order.profileId, rating.await()) }
        val subwayStation = async { getSubwayStation(order.subwayId) }
        val animal = async { getAnimal(order.animalId) }

        mapToGetOrderResponse(order, subwayStation.await(), profile.await(), animal.await())
    }

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

    private suspend fun getSubwayStation(subwayId: Int): SubwayStation =
        when (val response = subwayGrpcClient.getSubwayStation(subwayId)) {
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
