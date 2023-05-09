package ru.zveron.order.client.animal

import io.grpc.Status
import io.grpc.StatusException
import mu.KLogging
import net.logstash.logback.marker.Markers.append
import ru.zveron.contract.profile.AnimalServiceInternalGrpcKt
import ru.zveron.contract.profile.getAnimalRequestInt
import ru.zveron.order.client.animal.dto.GetAnimalApiResponse

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
}

