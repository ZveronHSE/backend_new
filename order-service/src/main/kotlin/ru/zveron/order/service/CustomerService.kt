package ru.zveron.order.service

import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import mu.KLogging
import org.springframework.stereotype.Service
import ru.zveron.order.client.address.GetSubwaysApiResponse
import ru.zveron.order.client.address.SubwayGrpcClient
import ru.zveron.order.client.animal.AnimalGrpcClient
import ru.zveron.order.client.animal.GetAnimalsApiResponse
import ru.zveron.order.client.profile.ProfileGrpcClient
import ru.zveron.order.client.profile.dto.GetProfileApiResponse
import ru.zveron.order.exception.ClientException
import ru.zveron.order.persistence.repository.OrderLotRepository
import ru.zveron.order.service.mapper.ModelMapper.of
import ru.zveron.order.service.mapper.ResponseMapper.toGetCustomerResponse
import ru.zveron.order.service.model.Animal
import ru.zveron.order.service.model.GetCustomerResponse
import ru.zveron.order.service.model.Profile
import ru.zveron.order.service.model.SubwayStation

@Service
class CustomerService(
    private val orderLotRepository: OrderLotRepository,
    private val subwayGrpcClient: SubwayGrpcClient,
    private val animalGrpcClient: AnimalGrpcClient,
    private val profileGrpcClient: ProfileGrpcClient,
) {

    companion object : KLogging()

    suspend fun getCustomer(customerId: Long): GetCustomerResponse {
        val orderLots = orderLotRepository.findAllByProfileId(customerId)

        return supervisorScope {
            val rating = async { getRating(customerId) }
            val profile = async { getProfile(customerId, rating.await()) }

            if (orderLots.isEmpty()) {
                return@supervisorScope toGetCustomerResponse(profile.await())
            }

            val subwayIdToValue = async { getSubways(orderLots.mapNotNull { it.subwayId }) }
            val animalIdToValue = async { getAnimals(orderLots.map { it.animalId }) }

            return@supervisorScope toGetCustomerResponse(
                profile.await(),
                orderLots,
                subwayIdToValue.await(),
                animalIdToValue.await(),
            )
        }
    }

    private suspend fun getSubways(subwayIds: List<Int>): Map<Int, SubwayStation?> {
        if (subwayIds.isEmpty()) return emptyMap()
        val clientResponse = subwayGrpcClient.getSubways(subwayIds)

        return when (clientResponse) {
            is GetSubwaysApiResponse.Error -> throw ClientException(clientResponse.message, clientResponse.error)
            is GetSubwaysApiResponse.Success -> clientResponse.subways.associateBy { it.id }
        }
    }

    private suspend fun getAnimals(animalIds: List<Long>): Map<Long, Animal?> {
        val clientResponse = animalGrpcClient.getAnimals(animalIds)

        return when (clientResponse) {
            is GetAnimalsApiResponse.Error -> throw ClientException(clientResponse.message, clientResponse.error)
            is GetAnimalsApiResponse.Success -> clientResponse.animals.associateBy { it.id }
        }
    }

    private suspend fun getRating(profileId: Long): Double = 4.5

    private suspend fun getProfile(profileId: Long, rating: Double): Profile {
        val clientResponse = profileGrpcClient.getProfile(profileId)

        return when (clientResponse) {
            is GetProfileApiResponse.Error -> throw ClientException(clientResponse.message, clientResponse.error)
            GetProfileApiResponse.NotFound -> throw ClientException.notFound()
            is GetProfileApiResponse.Success -> Profile.of(clientResponse.profile, rating)
        }
    }
}
