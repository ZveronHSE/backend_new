package ru.zveron.order.client.animal

import io.grpc.Status
import io.grpc.StatusException
import ru.zveron.contract.profile.AnimalServiceInternalGrpcKt
import ru.zveron.contract.profile.getAnimalRequestInt
import ru.zveron.order.client.animal.dto.GetAnimalApiResponse

class AnimalGrpcClient(
    private val animalGrpcStub: AnimalServiceInternalGrpcKt.AnimalServiceInternalCoroutineStub,
) {

    suspend fun getAnimal(animalId: Long): GetAnimalApiResponse {
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

