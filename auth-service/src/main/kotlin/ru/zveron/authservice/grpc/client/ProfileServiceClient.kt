package ru.zveron.authservice.grpc.client

import io.grpc.Status
import io.grpc.StatusException
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import ru.zveron.authservice.grpc.client.model.ProfileClientResponse
import ru.zveron.authservice.grpc.client.model.ProfileFound
import ru.zveron.authservice.grpc.client.model.ProfileNotFound
import ru.zveron.authservice.grpc.client.model.ProfileUnknownFailure
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
        "79257646188" to ProfileFound(123L, "vedro", "pomoyev"),
        "79996662233" to ProfileFound(124L, "player", "two")
    )

    private val idToProfile = mapOf(
        123L to ProfileFound(123L, "vedro", "pomoyev"),
        124L to ProfileFound(124L, "player", "two")
    )

    suspend fun getAccountByPhone(phoneNumber: String) =
        env.activeProfiles.singleOrNull { it.equals("local", true) }?.let {
            phoneNumberToProfile[phoneNumber] ?: ProfileNotFound
        } ?: getAccountByPhoneFromClient(phoneNumber)

    suspend fun getAccountByPhoneFromClient(phoneNumber: String): ProfileClientResponse {
        return try {
            val response = profileGrpcClient.getProfileByChannel(getProfileByChannelRequest {
                this.type = ChannelType.PHONE
                this.identifier = phoneNumber
            })
            return ProfileFound(response.id, response.name, response.surname)
        } catch (ex: StatusException) {
            when (ex.status) {
                Status.NOT_FOUND -> ProfileNotFound
                else -> ProfileUnknownFailure(ex.message, ex.status.code, ex.trailers)
            }
        }
    }

    suspend fun getProfileById(id: Long) =
        env.activeProfiles.singleOrNull { it.equals("local", true) }?.let {
            idToProfile[id] ?: ProfileNotFound
        } ?: getAccountByIdFromClient(id)

    suspend fun getAccountByIdFromClient(id: Long): ProfileClientResponse {
        return try {
            val response = profileGrpcClient.getProfile(getProfileRequest { this.id = id })
            return ProfileFound(response.id, response.name, response.surname)
        } catch (ex: StatusException) {
            when (ex.status) {
                Status.NOT_FOUND -> ProfileNotFound
                else -> ProfileUnknownFailure(ex.message, ex.status.code, ex.trailers)
            }
        }
    }
}
