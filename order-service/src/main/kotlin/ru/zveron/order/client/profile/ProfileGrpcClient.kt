package ru.zveron.order.client.profile

import io.grpc.Status
import io.grpc.StatusException
import ru.zveron.contract.profile.ProfileServiceInternalGrpcKt
import ru.zveron.contract.profile.getProfileRequest
import ru.zveron.order.client.profile.dto.GetProfileApiResponse

class ProfileGrpcClient(
    private val profileGrpcStub: ProfileServiceInternalGrpcKt.ProfileServiceInternalCoroutineStub,
) {

    suspend fun getProfile(profileId: Long): GetProfileApiResponse {
        val request = getProfileRequest { id = profileId }

        return try {
            val response = profileGrpcStub.getProfile(request)
            GetProfileApiResponse.Success(response)
        } catch (e: StatusException) {
            when (e.status.code) {
                Status.Code.NOT_FOUND -> GetProfileApiResponse.NotFound
                else -> GetProfileApiResponse.Error(e.status, e.message)
            }
        }
    }
}

