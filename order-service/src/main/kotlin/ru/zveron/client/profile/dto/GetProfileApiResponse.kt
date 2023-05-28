package ru.zveron.client.profile.dto

import io.grpc.Status
import ru.zveron.contract.profile.GetProfileResponse

sealed class GetProfileApiResponse {
    data class Success(val profile: GetProfileResponse) : GetProfileApiResponse()

    object NotFound : GetProfileApiResponse()

    data class Error(val error: Status, val message: String?) : GetProfileApiResponse()
}