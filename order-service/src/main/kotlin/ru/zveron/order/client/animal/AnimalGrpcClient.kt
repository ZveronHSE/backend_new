package ru.zveron.order.client.animal

import io.grpc.Status
import io.grpc.StatusException
import mu.KLogging
import net.logstash.logback.marker.Markers.append
import ru.zveron.contract.profile.AnimalServiceInternalGrpcKt
import ru.zveron.contract.profile.getAnimalBatchRequest
import ru.zveron.contract.profile.getAnimalRequestInt
import ru.zveron.order.client.animal.dto.GetAnimalApiResponse
import ru.zveron.order.service.mapper.ModelMapper.of
import ru.zveron.order.service.model.Animal

class AnimalGrpcClient(
        private val animalGrpcStub: AnimalServiceInternalGrpcKt.AnimalServiceInternalCoroutineStub,
) {

    companion object : KLogging()

    suspend fun getAnimal(animalId: Long): GetAnimalApiResponse {
        logger.debug(append("animalId", animalId)) { "Calling get animal from animal client" }

        val request = getAnimalRequestInt { this.animalId = animalId }

        return try {
            val response = animalGrpcStub.getAnimal(request)
            GetAnimalApiResponse.Success(response.animal)
        } catch (ex: StatusException) {
            when (ex.status.code) {
                Status.Code.NOT_FOUND -> GetAnimalApiResponse.NotFound
                else -> GetAnimalApiResponse.Error(ex.status, ex.message)
            }
        }
    }

    suspend fun getAnimals(animalIds: List<Long>): GetAnimalsApiResponse {
        logger.debug(append("animalIds", animalIds)) { "Calling get animals batch from animals client" }

        val request = getAnimalBatchRequest { this.animalIds.addAll(animalIds) }

        return try {
            val response = animalGrpcStub.getAnimalBatch(request)
            GetAnimalsApiResponse.Success(response.animalsList.map { Animal.of(it) })
        } catch (ex: StatusException) {
            GetAnimalsApiResponse.Error(ex.status, ex.message)
        }
    }
}
