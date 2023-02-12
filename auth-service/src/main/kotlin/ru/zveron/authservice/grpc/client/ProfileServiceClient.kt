package ru.zveron.authservice.grpc.client

import io.grpc.Status
import io.grpc.Status.Code
import io.grpc.StatusException
import org.springframework.core.env.Environment
import ru.zveron.authservice.grpc.client.model.PasswordIsInvalid
import ru.zveron.authservice.grpc.client.model.PasswordIsValid
import ru.zveron.authservice.grpc.client.model.PasswordValidationFailure
import ru.zveron.authservice.grpc.client.model.FindProfileResponse
import ru.zveron.authservice.grpc.client.model.ProfileFound
import ru.zveron.authservice.grpc.client.model.ProfileNotFound
import ru.zveron.authservice.grpc.client.model.ProfileUnknownFailure
import ru.zveron.authservice.grpc.client.model.RegisterProfileAlreadyExists
import ru.zveron.authservice.grpc.client.model.RegisterProfileByPhone
import ru.zveron.authservice.grpc.client.model.RegisterProfileFailure
import ru.zveron.authservice.grpc.client.model.RegisterProfileResponse
import ru.zveron.authservice.grpc.client.model.RegisterProfileSuccess
import ru.zveron.authservice.grpc.mapper.GrpcMapper.toClientRequest
import ru.zveron.authservice.grpc.mapper.GrpcMapper.toRequest
import ru.zveron.authservice.grpc.client.model.FindProfileUnknownFailure
import ru.zveron.authservice.grpc.client.model.ValidatePasswordProfileNotFound
import ru.zveron.authservice.grpc.client.model.ValidatePasswordRequest
import ru.zveron.authservice.grpc.client.model.ValidatePasswordResponse
import ru.zveron.contract.profile.ProfileServiceInternalGrpcKt
import ru.zveron.contract.profile.getProfileByChannelRequest
import ru.zveron.contract.profile.getProfileRequest
import ru.zveron.contract.profile.model.ChannelType
import ru.zveron.contract.profile.verifyProfileHashRequest

class ProfileServiceClient(
    private val profileGrpcClient: ProfileServiceInternalGrpcKt.ProfileServiceInternalCoroutineStub,
    private val env: Environment,
) {

    private val phoneNumberToProfile = mapOf(
        "79257646188" to ProfileFound(123L, "vedro", "pomoyev"),
        "79996662233" to ProfileFound(124L, "player", "two")
    )

    private val registerPhoneNumberToProfile = mapOf(
        "79993332211" to ProfileFound(1L, "vedro", "pomoyev"),
        "79996662233" to ProfileFound(124L, "player", "two")
    )

    private val idToProfile = mapOf(
        123L to ProfileFound(123L, "vedro", "pomoyev"),
        124L to ProfileFound(124L, "player", "two")
    )

    suspend fun getProfileByPhone(phoneNumber: String): FindProfileResponse =
        env.activeProfiles.singleOrNull { it.equals("local", true) }?.let {
            phoneNumberToProfile[phoneNumber] ?: ProfileNotFound
        } ?: getAccountByPhoneFromClient(phoneNumber)

    suspend fun getProfileById(id: Long) =
        env.activeProfiles.singleOrNull { it.equals("local", true) }?.let {
            idToProfile[id] ?: ProfileNotFound
        } ?: getAccountByIdFromClient(id)

    suspend fun registerProfileByPhone(request: RegisterProfileByPhone) =
        env.activeProfiles.singleOrNull { it.equals("local", true) }?.let {
            registerPhoneNumberToProfile[request.phone.toRequest()]?.id?.let { RegisterProfileSuccess(it) }
                ?: RegisterProfileAlreadyExists
        } ?: registerProfileByPhoneFromClient(request)

    suspend fun registerProfileByPhoneFromClient(request: RegisterProfileByPhone): RegisterProfileResponse = try {
        val response = profileGrpcClient.createProfile(request.toClientRequest())
        RegisterProfileSuccess(response.id)
    } catch (e: StatusException) {
        when (e.status.code) {
            Code.ALREADY_EXISTS -> RegisterProfileAlreadyExists
            else -> RegisterProfileFailure(e.message, e.status, e.trailers)
        }
    }

    suspend fun getAccountByPhoneFromClient(phoneNumber: String): FindProfileResponse {
        return try {
            val response = profileGrpcClient.getProfileByChannel(getProfileByChannelRequest {
                this.type = ChannelType.PHONE
                this.identifier = phoneNumber
            })
            return ProfileFound(response.id, response.name, response.surname)
        } catch (ex: StatusException) {
            when (ex.status) {
                Status.NOT_FOUND -> ProfileNotFound
                else -> FindProfileUnknownFailure(ex.message, ex.status.code, ex.trailers)
            }
        }
    }

    suspend fun validatePassword(request: ValidatePasswordRequest): ValidatePasswordResponse {
        return try {
            val response = profileGrpcClient.verifyProfileHash(verifyProfileHashRequest {
                this.passwordHash = request.passwordHash
                this.phoneNumber = request.phoneNumber
            })

            if (response.isValidRequest) PasswordIsValid else PasswordIsInvalid
        } catch (ex: StatusException) {
            if (ex.status == Status.NOT_FOUND) {
                ValidatePasswordProfileNotFound
            } else {
                PasswordValidationFailure(ex.message, ex.status, ex.trailers)
            }
        }
    }

    suspend fun getProfileById(id: Long) =
        env.activeProfiles.singleOrNull { it.equals("local", true) }?.let {
            idToProfile[id] ?: ValidatePasswordProfileNotFound
        } ?: getAccountByIdFromClient(id)

    suspend fun getAccountByIdFromClient(id: Long): FindProfileResponse {
        return try {
            val response = profileGrpcClient.getProfile(getProfileRequest { this.id = id })
            return ProfileFound(response.id, response.name, response.surname)
        } catch (ex: StatusException) {
            when (ex.status) {
                Status.NOT_FOUND -> ProfileNotFound
                else -> FindProfileUnknownFailure(ex.message, ex.status.code, ex.trailers)
            }
        }
    }
}

