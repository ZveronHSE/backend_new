package ru.zveron.client.profile

import io.grpc.Status
import io.grpc.StatusException
import org.springframework.stereotype.Service
import ru.zveron.contract.profile.ProfileServiceInternalGrpcKt
import ru.zveron.contract.profile.getProfileWithContactsRequest
import ru.zveron.exception.LotException
import ru.zveron.mapper.SellerMapper.toSellerProfile
import ru.zveron.model.SellerProfile

@Service
class ProfileClient(
    private val profileStub: ProfileServiceInternalGrpcKt.ProfileServiceInternalCoroutineStub
) {
    suspend fun getProfileWithContacts(sellerId: Long): SellerProfile {
        val request = getProfileWithContactsRequest {
            id = sellerId
        }

        return try {
            val response = profileStub.getProfileWithContacts(request)

            response.toSellerProfile()
        } catch (ex: StatusException) {
            throw LotException(
                Status.INTERNAL,
                "Can't get profile by id: $sellerId. Status: ${ex.status.description}"
            )
        }
    }
}
