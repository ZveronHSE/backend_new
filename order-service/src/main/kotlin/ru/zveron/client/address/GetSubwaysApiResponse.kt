package ru.zveron.client.address

import io.grpc.Status
import ru.zveron.service.model.SubwayStation

sealed class GetSubwaysApiResponse {
    data class Success(val subways: List<SubwayStation>) : ru.zveron.client.address.GetSubwaysApiResponse()
    data class Error(val error: Status, val message: String?) : ru.zveron.client.address.GetSubwaysApiResponse()
}