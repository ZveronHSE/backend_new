package ru.zveron.service

import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import mu.KLogging
import org.springframework.stereotype.Service
import ru.zveron.client.address.GetSubwaysApiResponse
import ru.zveron.client.address.SubwayGrpcClient
import ru.zveron.client.animal.AnimalGrpcClient
import ru.zveron.client.animal.GetAnimalsApiResponse
import ru.zveron.client.profile.ProfileGrpcClient
import ru.zveron.client.profile.dto.GetProfileApiResponse
import ru.zveron.exception.ClientException
import ru.zveron.persistence.repository.OrderLotRepository
import ru.zveron.service.mapper.ModelMapper.of
import ru.zveron.service.mapper.ResponseMapper.toGetCustomerResponse
import ru.zveron.service.model.Animal
import ru.zveron.service.model.GetCustomerResponse
import ru.zveron.service.model.Profile
import ru.zveron.service.model.SubwayStation

@Service
class CustomerService(
    private val orderLotRepository: OrderLotRepository,
    private val subwayGrpcClient: ru.zveron.client.address.SubwayGrpcClient,
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

    //todo: move to decorator
    private suspend fun getSubways(subwayIds: List<Int>): Map<Int, SubwayStation?> {
        if (subwayIds.isEmpty()) return emptyMap()
        val clientResponse = subwayGrpcClient.getSubways(subwayIds)

        return when (clientResponse) {
            is ru.zveron.client.address.GetSubwaysApiResponse.Error -> throw ClientException(clientResponse.message, clientResponse.error)
            is ru.zveron.client.address.GetSubwaysApiResponse.Success -> clientResponse.subways.associateBy { it.id }
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
