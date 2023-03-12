package ru.zveron.authservice.grpc.client

import io.grpc.Status.Code
import io.grpc.StatusException
import ru.zveron.authservice.component.thirdparty.contant.ThirdPartyProviderType
import ru.zveron.authservice.grpc.client.model.FindProfileResponse
import ru.zveron.authservice.grpc.client.model.FindProfileUnknownFailure
import ru.zveron.authservice.grpc.client.model.PasswordIsInvalid
import ru.zveron.authservice.grpc.client.model.PasswordIsValid
import ru.zveron.authservice.grpc.client.model.PasswordValidationFailure
import ru.zveron.authservice.grpc.client.model.ProfileFound
import ru.zveron.authservice.grpc.client.model.ProfileNotFound
import ru.zveron.authservice.grpc.client.model.RegisterByPhoneRequest
import ru.zveron.authservice.grpc.client.model.RegisterBySocialMediaRequest
import ru.zveron.authservice.grpc.client.model.RegisterProfileAlreadyExists
import ru.zveron.authservice.grpc.client.model.RegisterProfileFailure
import ru.zveron.authservice.grpc.client.model.RegisterProfileResponse
import ru.zveron.authservice.grpc.client.model.RegisterProfileSuccess
import ru.zveron.authservice.grpc.client.model.ValidatePasswordRequest
import ru.zveron.authservice.grpc.client.model.ValidatePasswordResponse
import ru.zveron.authservice.grpc.mapper.GrpcMapper.toClientRequest
import ru.zveron.contract.profile.ProfileServiceInternalGrpcKt
import ru.zveron.contract.profile.getProfileByChannelRequest
import ru.zveron.contract.profile.getProfileRequest
import ru.zveron.contract.profile.model.ChannelType
import ru.zveron.contract.profile.verifyProfileHashRequest

class ProfileServiceClient(
    private val profileGrpcClient: ProfileServiceInternalGrpcKt.ProfileServiceInternalCoroutineStub,
) {

    suspend fun registerProfileByPhone(request: RegisterByPhoneRequest): RegisterProfileResponse = try {
        val response = profileGrpcClient.createProfile(request.toClientRequest())
        RegisterProfileSuccess(response.id)
    } catch (e: StatusException) {
        when (e.status.code) {
            Code.ALREADY_EXISTS -> RegisterProfileAlreadyExists
            else -> RegisterProfileFailure(e.message, e.status, e.trailers)
        }
    }

    suspend fun registerProfileBySocialMedia(request: RegisterBySocialMediaRequest): RegisterProfileResponse = try {
        val response = profileGrpcClient.createProfile(request.toClientRequest())

        RegisterProfileSuccess(response.id)
    } catch (e: StatusException) {
        when (e.status.code) {
            Code.ALREADY_EXISTS -> RegisterProfileAlreadyExists
            else -> RegisterProfileFailure(e.message, e.status, e.trailers)
        }
    }

    suspend fun getProfileByPhone(phoneNumber: String): FindProfileResponse {
        return try {
            val response = profileGrpcClient.getProfileByChannel(getProfileByChannelRequest {
                this.type = ChannelType.PHONE
                this.identifier = phoneNumber
            })
            return ProfileFound(response.id, response.name, response.surname)
        } catch (ex: StatusException) {
            when (ex.status.code) {
                Code.NOT_FOUND -> ProfileNotFound
                else -> FindProfileUnknownFailure(ex.message, ex.status.code, ex.trailers)
            }
        }
    }

    suspend fun findProfileBySocialMedia(
        providerType: ThirdPartyProviderType,
        providerUserId: String,
    ) = try {
        val request = getProfileByChannelRequest {
            this.type = providerType.toProfileClientType()
            this.identifier = providerUserId
        }

        val response = profileGrpcClient.getProfileByChannel(request)

        ProfileFound(response.id, response.name, response.surname)
    } catch (ex: StatusException) {
        when (ex.status.code) {
            Code.NOT_FOUND -> ProfileNotFound
            else -> FindProfileUnknownFailure(ex.message, ex.status.code, ex.trailers)
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
            PasswordValidationFailure(ex.message, ex.status, ex.trailers)
        }
    }

    suspend fun findProfileById(id: Long): FindProfileResponse {
        return try {
            val response = profileGrpcClient.getProfile(getProfileRequest { this.id = id })

            return ProfileFound(response.id, response.name, response.surname)
        } catch (ex: StatusException) {
            when (ex.status.code) {
                Code.NOT_FOUND -> ProfileNotFound
                else -> FindProfileUnknownFailure(ex.message, ex.status.code, ex.trailers)
            }
        }
    }
}

fun ThirdPartyProviderType.toProfileClientType() = when (this) {
    ThirdPartyProviderType.GMAIL -> ChannelType.GOOGLE
    ThirdPartyProviderType.VK -> ChannelType.VK
}
