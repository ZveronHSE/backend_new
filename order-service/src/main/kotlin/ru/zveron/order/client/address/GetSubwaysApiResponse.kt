package ru.zveron.order.client.address

import io.grpc.Status
import ru.zveron.order.service.model.SubwayStation

sealed class GetSubwaysApiResponse {
    data class Success(val subways: List<SubwayStation>) : GetSubwaysApiResponse()
    data class Error(val error: Status, val message: String?) : GetSubwaysApiResponse()
}