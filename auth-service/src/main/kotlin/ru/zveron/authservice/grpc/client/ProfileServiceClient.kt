package ru.zveron.authservice.grpc.client

import io.grpc.Status
import io.grpc.StatusException
import org.springframework.stereotype.Service
import ru.zveron.authservice.grpc.client.dto.ProfileClientResponse
import ru.zveron.authservice.grpc.client.dto.ProfileFound
import ru.zveron.authservice.grpc.client.dto.ProfileNotFound
import ru.zveron.authservice.grpc.client.dto.ProfileUnknownFailure


@Service
class ProfileServiceClient {
    suspend fun getAccountByPhone(phoneNumber: ru.zveron.authservice.persistence.model.PhoneNumber): ProfileClientResponse {
        //todo implement once the endpoint is ready
        return try {
            if (phoneNumber.phone == "9257646188") {
                throw StatusException(Status.NOT_FOUND)
            }
            return ProfileFound(111, "Kek", "Pikek")
        } catch (ex: StatusException) {
            when (val code = ex.status.code) {
                Status.Code.NOT_FOUND -> ProfileNotFound
                else -> ProfileUnknownFailure(message = ex.message, code = code, metadata = ex.trailers)
            }
        }
    }
}
