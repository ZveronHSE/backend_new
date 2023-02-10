package ru.zveron.service.api

import com.google.protobuf.Empty
import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.contract.profile.CreateProfileRequest
import ru.zveron.contract.profile.GetProfileByChannelRequest
import ru.zveron.contract.profile.GetProfileByChannelResponse
import ru.zveron.contract.profile.GetProfileRequest
import ru.zveron.contract.profile.GetProfileResponse
import ru.zveron.contract.profile.GetProfileWithContactsRequest
import ru.zveron.contract.profile.GetProfileWithContactsResponse
import ru.zveron.contract.profile.ProfileServiceInternalGrpcKt
import ru.zveron.contract.profile.UpdateContactsRequest
import ru.zveron.service.CommunicationLinkService
import ru.zveron.service.ProfileService

@GrpcService
class ProfileServiceInternal(
    private val communicationLinkService: CommunicationLinkService,
    private val profileService: ProfileService,
) : ProfileServiceInternalGrpcKt.ProfileServiceInternalCoroutineImplBase() {

    override suspend fun createProfile(request: CreateProfileRequest): Empty {
        profileService.createProfile(request)
        return Empty.getDefaultInstance()
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
}