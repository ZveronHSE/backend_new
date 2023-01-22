package ru.zv.authservice.grpc

import io.grpc.Metadata
import io.grpc.Status
import io.grpc.StatusException
import org.apache.commons.lang3.RandomUtils
import ru.zveron.ProfileServiceInternalGrpcKt
import ru.zveron.getProfileRequest

class ProfileServiceClient(
    private val profileServiceStub: ProfileServiceInternalGrpcKt.ProfileServiceInternalCoroutineStub,
) {
    suspend fun getAccountByPhone(phoneNumber: ru.zv.authservice.persistence.model.PhoneNumber): ProfileClientResponse {
        //todo implement once the endpoint is ready
        return try {
            profileServiceStub.getProfile(getProfileRequest {
                this.id = RandomUtils.nextLong()
            }).let {
                ProfileFound(it.id, it.name, it.surname)
            }
        } catch (ex: StatusException) {
            when (val code = ex.status.code) {
                Status.Code.NOT_FOUND -> ProfileNotFound
                else -> ProfileUnknownFailure(message = ex.message, code = code, metadata = ex.trailers)
            }
        }
    }
}

sealed class ProfileClientResponse

data class ProfileFound(
    val id: Long,
    val name: String,
    val surname: String,
) : ProfileClientResponse()

object ProfileNotFound : ProfileClientResponse()

data class ProfileUnknownFailure(
    val message: String?,
    val code: Status.Code,
    val metadata: Metadata,
) : ProfileClientResponse()
