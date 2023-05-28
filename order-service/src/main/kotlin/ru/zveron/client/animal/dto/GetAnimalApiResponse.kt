package ru.zveron.client.animal.dto

import io.grpc.Status
import ru.zveron.contract.profile.model.FullAnimal

sealed class GetAnimalApiResponse {
    data class Success(val animal: FullAnimal) : GetAnimalApiResponse()

    object NotFound : GetAnimalApiResponse()

    data class Error(val error: Status, val message: String?) : GetAnimalApiResponse()
}