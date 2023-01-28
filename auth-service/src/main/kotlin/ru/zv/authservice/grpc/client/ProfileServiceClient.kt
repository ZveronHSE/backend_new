package ru.zv.authservice.grpc.client

import io.grpc.Status
import io.grpc.StatusException
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import ru.zv.authservice.grpc.client.dto.ProfileClientResponse
import ru.zv.authservice.grpc.client.dto.ProfileFound
import ru.zv.authservice.grpc.client.dto.ProfileNotFound
import ru.zv.authservice.grpc.client.dto.ProfileUnknownFailure
import ru.zv.authservice.persistence.model.PhoneNumber
import ru.zv.authservice.persistence.model.toClient
import ru.zveron.contract.profile.ProfileServiceInternalGrpcKt
import ru.zveron.contract.profile.getProfileByChannelRequest
import ru.zveron.contract.profile.getProfileRequest
import ru.zveron.contract.profile.model.ChannelType

@Service
class ProfileServiceClient() {

    @GrpcClient("grpc-profile-client")
    lateinit var profileGrpcClient: ProfileServiceInternalGrpcKt.ProfileServiceInternalCoroutineStub

    @Autowired
    lateinit var env: Environment

    private val phoneNumberToProfile = mapOf(
        PhoneNumber("7", "9257646188") to ProfileFound(123L, "vedro", "pomoyev"),
        PhoneNumber("7", "9996662233") to ProfileFound(124L, "player", "two")
    )

    private val idToProfile = mapOf(
        123L to ProfileFound(123L, "vedro", "pomoyev"),
        124L to ProfileFound(124L, "player", "two")
    )

    suspend fun getAccountByPhone(phoneNumber: PhoneNumber) =
        env.activeProfiles.singleOrNull { it.equals("local", true) }?.let {
            phoneNumberToProfile[phoneNumber] ?: ProfileNotFound
        } ?: getAccountByPhoneFromClient(phoneNumber)

    suspend fun getProfileById(id: Long) =
        env.activeProfiles.singleOrNull { it.equals("local", true) }?.let {
            idToProfile[id] ?: ProfileNotFound
        } ?: getAccountByIdFromClient(id)

    suspend fun getAccountByIdFromClient(id: Long): ProfileClientResponse {
        return try {
            val stubResponse = profileGrpcClient.getProfile(request = getProfileRequest {
                this.id = id
            })
            return ProfileFound(stubResponse.id, stubResponse.name, stubResponse.surname)
        } catch (ex: StatusException) {
            when (val code = ex.status.code) {
                Status.Code.NOT_FOUND -> ProfileNotFound
                else -> ProfileUnknownFailure(message = ex.message, code = code, metadata = ex.trailers)
            }
        }
    }

    suspend fun getAccountByPhoneFromClient(phoneNumber: PhoneNumber): ProfileClientResponse {
        return try {
            val stubResponse = profileGrpcClient.getProfileByChannel(request = getProfileByChannelRequest {
                this.type = ChannelType.PHONE
                this.identifier = phoneNumber.toClient()
            })
            return ProfileFound(stubResponse.id, stubResponse.name, stubResponse.surname)
        } catch (ex: StatusException) {
            when (val code = ex.status.code) {
                Status.Code.NOT_FOUND -> ProfileNotFound
                else -> ProfileUnknownFailure(message = ex.message, code = code, metadata = ex.trailers)
            }
        }
    }
}
