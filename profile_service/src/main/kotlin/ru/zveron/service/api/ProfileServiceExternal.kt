package ru.zveron.service.api

import com.google.protobuf.Empty
import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.contract.profile.GetChannelTypesResponse
import ru.zveron.contract.profile.GetProfileInfoResponse
import ru.zveron.contract.profile.GetProfilePageRequest
import ru.zveron.contract.profile.GetProfilePageResponse
import ru.zveron.contract.profile.GetSettingsResponse
import ru.zveron.contract.profile.ProfileServiceExternalGrpcKt
import ru.zveron.contract.profile.SetProfileInfoRequest
import ru.zveron.contract.profile.SetSettingsRequest
import ru.zveron.contract.profile.model.Links
import ru.zveron.exception.ProfileUnauthenticated
import ru.zveron.library.grpc.util.GrpcUtils
import ru.zveron.mapper.ContactsMapper.toLinks
import ru.zveron.service.CommunicationLinkService
import ru.zveron.service.ProfileService
import ru.zveron.service.SettingsService
import kotlin.coroutines.coroutineContext

@GrpcService
class ProfileServiceExternal(
    private val communicationLinkService: CommunicationLinkService,
    private val profileService: ProfileService,
    private val settingsService: SettingsService,
) : ProfileServiceExternalGrpcKt.ProfileServiceExternalCoroutineImplBase() {

    override suspend fun getProfilePage(request: GetProfilePageRequest): GetProfilePageResponse =
        profileService.getProfilePage(request, getAuthorizedProfileId() ?: 0)

    override suspend fun getProfileInfo(request: Empty): GetProfileInfoResponse =
        profileService.getProfileInfo(
            getAuthorizedProfileId()
                ?: throw ProfileUnauthenticated("Authentication required")
        )

    override suspend fun setProfileInfo(request: SetProfileInfoRequest): Empty {
        profileService.setProfileInfo(
            request,
            getAuthorizedProfileId()
                ?: throw ProfileUnauthenticated("Authentication required")
        )
        return Empty.getDefaultInstance()
    }

    override suspend fun getChannelTypes(request: Empty): GetChannelTypesResponse =
        settingsService.getChannelTypes(
            getAuthorizedProfileId() ?: throw ProfileUnauthenticated("Authentication required")
        )

    override suspend fun getLinks(request: Empty): Links =
        communicationLinkService
            .findByIdOrThrow(
                getAuthorizedProfileId() ?: throw ProfileUnauthenticated("Authentication required")
            )
            .toLinks()

    override suspend fun getSettings(request: Empty): GetSettingsResponse =
        settingsService.getSettings(
            getAuthorizedProfileId()
                ?: throw ProfileUnauthenticated("Authentication required")
        )

    override suspend fun setSettings(request: SetSettingsRequest): Empty {
        settingsService.setSettings(
            request,
            getAuthorizedProfileId()
                ?: throw ProfileUnauthenticated("Authentication required"),
        )
        return Empty.getDefaultInstance()
    }

    override suspend fun deleteProfile(request: Empty): Empty {
        val id = getAuthorizedProfileId() ?: throw ProfileUnauthenticated("Authentication required")
        profileService.deleteById(id)
        // TODO: Add message to kafka
        return Empty.getDefaultInstance()
    }

    private suspend fun getAuthorizedProfileId() =
        GrpcUtils.getMetadata(coroutineContext, requiredAuthorized = false).profileId
}