package ru.zveron.service.api

import com.google.protobuf.Empty
import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.contract.profile.DeleteProfileRequest
import ru.zveron.contract.profile.GetChannelTypesRequest
import ru.zveron.contract.profile.GetChannelTypesResponse
import ru.zveron.contract.profile.GetLinksRequest
import ru.zveron.contract.profile.GetProfileInfoRequest
import ru.zveron.contract.profile.GetProfileInfoResponse
import ru.zveron.contract.profile.GetProfilePageRequest
import ru.zveron.contract.profile.GetProfilePageResponse
import ru.zveron.contract.profile.GetSettingsRequest
import ru.zveron.contract.profile.GetSettingsResponse
import ru.zveron.contract.profile.ProfileServiceExternalGrpcKt
import ru.zveron.contract.profile.SetProfileInfoRequest
import ru.zveron.contract.profile.SetSettingsRequest
import ru.zveron.contract.profile.model.Links
import ru.zveron.mapper.ContactsMapper.toModel
import ru.zveron.service.ContactService
import ru.zveron.service.ProfileService
import ru.zveron.service.SettingsService

@GrpcService
class ProfileServiceExternal(
    private val contactService: ContactService,
    private val profileService: ProfileService,
    private val settingsService: SettingsService,
) : ProfileServiceExternalGrpcKt.ProfileServiceExternalCoroutineImplBase() {

    override suspend fun getProfilePage(request: GetProfilePageRequest): GetProfilePageResponse =
        profileService.getProfilePage(request)

    override suspend fun getProfileInfo(request: GetProfileInfoRequest): GetProfileInfoResponse =
        profileService.getProfileInfo(request)

    override suspend fun setProfileInfo(request: SetProfileInfoRequest): Empty {
        profileService.setProfileInfo(request)
        return Empty.getDefaultInstance()
    }

    override suspend fun getChannelTypes(request: GetChannelTypesRequest): GetChannelTypesResponse =
        settingsService.getChannelTypes(request)

    override suspend fun getLinks(request: GetLinksRequest): Links =
        contactService.findByIdOrThrow(request.id).toModel()

    override suspend fun getSettings(request: GetSettingsRequest): GetSettingsResponse =
        settingsService.getSettings(request)

    override suspend fun setSettings(request: SetSettingsRequest): Empty {
        settingsService.setSettings(request)
        return Empty.getDefaultInstance()
    }

    override suspend fun deleteProfile(request: DeleteProfileRequest): Empty {
        profileService.deleteById(request.id)
        // TODO: Add message to kafka
        return Empty.getDefaultInstance()
    }
}
