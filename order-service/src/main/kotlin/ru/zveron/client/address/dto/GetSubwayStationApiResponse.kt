package ru.zveron.client.address.dto

import io.grpc.Status
import ru.zveron.contract.address.internal.SubwayStationInt

sealed class GetSubwayStationApiResponse {
    data class Success(val subwayStation: SubwayStationInt) : GetSubwayStationApiResponse()

    object NotFound : GetSubwayStationApiResponse()

    data class Error(val error: Status, val message: String?) : GetSubwayStationApiResponse()
}