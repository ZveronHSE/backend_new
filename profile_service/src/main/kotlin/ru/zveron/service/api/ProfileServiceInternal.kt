package ru.zveron.service.api

import com.google.protobuf.Empty
import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.contract.profile.CreateProfileRequest
import ru.zveron.contract.profile.CreateProfileResponse
import ru.zveron.contract.profile.ExistsByIdRequest
import ru.zveron.contract.profile.ExistsByIdResponse
import ru.zveron.contract.profile.GetProfileByChannelRequest
import ru.zveron.contract.profile.GetProfileByChannelResponse
import ru.zveron.contract.profile.GetProfileRequest
import ru.zveron.contract.profile.GetProfileResponse
import ru.zveron.contract.profile.GetProfileWithContactsRequest
import ru.zveron.contract.profile.GetProfileWithContactsResponse
import ru.zveron.contract.profile.GetProfilesSummaryRequest
import ru.zveron.contract.profile.GetProfilesSummaryResponse
import ru.zveron.contract.profile.ProfileServiceInternalGrpcKt
import ru.zveron.contract.profile.UpdateContactsRequest
import ru.zveron.contract.profile.VerifyProfileHashRequest
import ru.zveron.contract.profile.VerifyProfileHashResponse
import ru.zveron.contract.profile.createProfileResponse
import ru.zveron.contract.profile.existsByIdResponse
import ru.zveron.contract.profile.verifyProfileHashResponse
import ru.zveron.service.CommunicationLinkService
import ru.zveron.service.ProfileService

@GrpcService
class ProfileServiceInternal(
    private val communicationLinkService: CommunicationLinkService,
    private val profileService: ProfileService,
) : ProfileServiceInternalGrpcKt.ProfileServiceInternalCoroutineImplBase() {

    override suspend fun createProfile(request: CreateProfileRequest): CreateProfileResponse =
        createProfileResponse {
            id = profileService.createProfile(request)
        }

    override suspend fun getProfile(request: GetProfileRequest): GetProfileResponse =
        profileService.getProfile(request)


    override suspend fun getProfileWithContacts(request: GetProfileWithContactsRequest): GetProfileWithContactsResponse =
        profileService.getProfileWithContacts(request)

    override suspend fun updateContacts(request: UpdateContactsRequest): Empty {
        communicationLinkService.updateContacts(request)
        return Empty.getDefaultInstance()
    }

    override suspend fun getProfileByChannel(request: GetProfileByChannelRequest): GetProfileByChannelResponse =
        communicationLinkService.getProfileByChannel(request)

    override suspend fun verifyProfileHash(request: VerifyProfileHashRequest): VerifyProfileHashResponse =
        verifyProfileHashResponse {
            isValidRequest = communicationLinkService.isPasswordHashValid(request)
        }

    override suspend fun getProfilesSummary(request: GetProfilesSummaryRequest): GetProfilesSummaryResponse =
        profileService.getProfileSummary(request)

    override suspend fun existsById(request: ExistsByIdRequest): ExistsByIdResponse =
        existsByIdResponse { exists = profileService.existsById(request.id) }

    override suspend fun getProfileForOrder(request: GetProfileRequest) = profileService.getProfileForOrder(request)
}