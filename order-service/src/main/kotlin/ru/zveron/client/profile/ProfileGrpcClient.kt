package ru.zveron.client.profile

import io.grpc.Status
import io.grpc.StatusException
import mu.KLogging
import net.logstash.logback.marker.Markers.append
import ru.zveron.contract.profile.ProfileServiceInternalGrpcKt
import ru.zveron.contract.profile.getProfileRequest
import ru.zveron.client.profile.dto.GetProfileApiResponse

class ProfileGrpcClient(
    private val profileGrpcStub: ProfileServiceInternalGrpcKt.ProfileServiceInternalCoroutineStub,
) {

    companion object : KLogging()

    suspend fun getProfile(profileId: Long): GetProfileApiResponse {
        logger.debug(append("profileId", profileId)) { "Calling get profile from profile client" }

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
