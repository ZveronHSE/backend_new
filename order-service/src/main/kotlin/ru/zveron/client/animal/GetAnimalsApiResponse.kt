package ru.zveron.client.animal

import io.grpc.Status
import ru.zveron.service.model.Animal

sealed class GetAnimalsApiResponse {
    data class Success(val animals: List<Animal>) : GetAnimalsApiResponse()
    data class Error(val error: Status, val message: String?) : GetAnimalsApiResponse()
}