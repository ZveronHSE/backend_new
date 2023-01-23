package ru.zveron.service.api.profile

import com.google.protobuf.Empty
import com.google.protobuf.timestamp
import io.grpc.StatusException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import mu.KLogging
import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.ChannelType
import ru.zveron.DeleteProfileRequest
import ru.zveron.GetChannelTypesRequest
import ru.zveron.GetChannelTypesResponse
import ru.zveron.GetLinksRequest
import ru.zveron.GetProfileInfoRequest
import ru.zveron.GetProfileInfoResponse
import ru.zveron.GetProfilePageRequest
import ru.zveron.GetProfilePageResponse
import ru.zveron.GetSettingsRequest
import ru.zveron.GetSettingsResponse
import ru.zveron.Links
import ru.zveron.LotStatus
import ru.zveron.ProfileServiceExternalGrpcKt
import ru.zveron.SetProfileInfoRequest
import ru.zveron.SetSettingsRequest
import ru.zveron.address
import ru.zveron.mapper.AddressMapper
import ru.zveron.mapper.AddressMapper.toProfileAddress
import ru.zveron.mapper.ContactsMapper
import ru.zveron.mapper.ContactsMapper.channelsDTO2Model
import ru.zveron.mapper.LotsMapper
import ru.zveron.validation.ContactsValidator.validateLinksNotBlank
import ru.zveron.validation.ContactsValidator.validateNumberOfChannels
import ru.zveron.contacts
import ru.zveron.entity.Profile
import ru.zveron.exception.ProfileException
import ru.zveron.getChannelTypesResponse
import ru.zveron.getSettingsResponse
import ru.zveron.service.ContactService
import ru.zveron.service.ProfileService
import ru.zveron.service.SettingsService
import ru.zveron.service.api.address.AddressService
import ru.zveron.service.api.blakclist.BlacklistService
import ru.zveron.service.api.lot.LotService
import ru.zveron.service.api.review.ReviewService

@GrpcService
class ProfileServiceExternal(
    private val addressService: AddressService,
    private val blacklistService: BlacklistService,
    private val contactService: ContactService,
    private val lotService: LotService,
    private val profileService: ProfileService,
    private val reviewService: ReviewService,
    private val settingsService: SettingsService,
) : ProfileServiceExternalGrpcKt.ProfileServiceExternalCoroutineImplBase() {

    companion object : KLogging()

    override suspend fun getProfilePage(request: GetProfilePageRequest): GetProfilePageResponse {
        val profile = profileService.findByIdOrThrow(request.requestedProfileId)
        val responseBuilder = GetProfilePageResponse.newBuilder().apply {
            id = profile.id
            name = profile.name
            surname = profile.surname
            imageId = profile.imageId
            lastActivity = timestamp { seconds = profile.lastSeen.epochSecond; nanos = profile.lastSeen.nano }
        }

        if (request.authorizedProfileId == 0L || !blacklistService.existsInBlacklist(
                ownerId = request.requestedProfileId,
                targetUserId = request.authorizedProfileId
            )
        ) {
            addAdditionalFields(profile, responseBuilder)
        }

        return responseBuilder.build()
    }

    override suspend fun getProfileInfo(request: GetProfileInfoRequest): GetProfileInfoResponse {
        val profile = profileService.findByIdOrThrow(request.id)
        val response = GetProfileInfoResponse.newBuilder().apply {
            id = profile.id
            name = profile.name
            surname = profile.surname
            imageId = profile.imageId
        }

        coroutineScope {
            val addressCoroutine = async(CoroutineName("Get-Address-Coroutine")) {
                if (profile.addressId != 0L) addressService.getById(profile.addressId) else null
            }
            val reviewCoroutine = async(CoroutineName("Get-Review-Coroutine")) {
                reviewService.getRating(profile.id)
            }
            try {
                response.rating = reviewCoroutine.await()
            } catch (ex: StatusException) {
                logger.error(ex.message)
            }
            try {
                addressCoroutine.await()?.let { response.address = AddressMapper.response2Address(it) }
            } catch (ex: StatusException) {
                logger.error(ex.message)
            }
        }

        return response.build()
    }

    override suspend fun setProfileInfo(request: SetProfileInfoRequest): Empty {
        val profile = profileService.findByIdOrThrow(request.id)
        val newAddress = addressService.saveIfNotExists(AddressMapper.address2Request(request.address))
        profile.apply {
            name = request.name
            surname = request.surname
            imageId = request.imageId
            addressId = newAddress.id
        }

        profileService.save(profile)
        return Empty.getDefaultInstance()
    }

    override suspend fun getChannelTypes(request: GetChannelTypesRequest): GetChannelTypesResponse =
        getChannelTypesResponse {
            val settings = settingsService.findByIdOrThrow(request.id)
            channels.addAll(channelsDTO2Model(settings.channels))
        }

    override suspend fun getLinks(request: GetLinksRequest): Links =
        ContactsMapper.linksEntity2Model(contactService.findByIdOrThrow(request.id))

    override suspend fun getSettings(request: GetSettingsRequest): GetSettingsResponse =
        getSettingsResponse {
            val settings = settingsService.findByIdOrThrow(request.id)
            val address = addressService.getById(settings.searchAddressId)
            channels.addAll(channelsDTO2Model(settings.channels))
            this.address = address {
                region = address.region
                town = address.town
                latitude = address.latitude
                longitude = address.longitude
            }
        }

    override suspend fun setSettings(request: SetSettingsRequest): Empty {
        val settings = settingsService.findByIdOrThrow(request.id)
        val links = settings.profile.contact
        if (request.channelsList.contains(ChannelType.VK) && links.vkRef.isBlank()) {
            throw ProfileException("Can't use vk as communication channel because link is missed")
        }
        if (request.channelsList.contains(ChannelType.GOOGLE) && links.gmail.isBlank()) {
            throw ProfileException("Can't use gmail as communication channel because link is missed")
        }
        if (request.channelsList.contains(ChannelType.PHONE) && links.phone.isBlank()) {
            throw ProfileException("Can't use phone as communication channel because link is missed")
        }
        settings.channels = ContactsMapper.channelsModel2DTO(request.channelsList.toSet())
        validateNumberOfChannels(settings.channels)
        validateLinksNotBlank(settings.channels, links)
        settings.searchAddressId = addressService.saveIfNotExists(AddressMapper.address2Request(request.address)).id
        settingsService.save(settings)

        return Empty.getDefaultInstance()
    }

    override suspend fun deleteProfile(request: DeleteProfileRequest): Empty {
        profileService.deleteById(request.id)
        // TODO: Add message to kafka

        return Empty.getDefaultInstance()
    }

    private suspend fun addAdditionalFields(profile: Profile, responseBuilder: GetProfilePageResponse.Builder) =
        coroutineScope {
            val lotsCoroutine = async(CoroutineName("Get-Lots-Coroutine")) { lotService.getLotsBySellerId(profile.id) }
            val addressCoroutine = async(CoroutineName("Get-Address-Coroutine")) {
                if (profile.addressId != 0L) addressService.getById(profile.addressId) else null
            }
            val reviewCoroutine = async(CoroutineName("Get-Review-Coroutine")) {
                reviewService.getRating(profile.id)
            }

            responseBuilder.contacts = contacts {
                links = ContactsMapper.linksEntity2Model(profile.contact)
                channels.addAll(channelsDTO2Model(profile.settings.channels))
            }

            try {
                val address = addressCoroutine.await()
                responseBuilder.address = address?.toProfileAddress()
            } catch (ex: StatusException) {
                logger.error(ex.message)
            }

            try {
                val lots = lotsCoroutine.await()
                responseBuilder.addAllActiveLots(
                    LotsMapper.lot2Builder(
                        lots.activateLotsList,
                        LotStatus.ACTIVE
                    )
                )
                responseBuilder.addAllClosedLots(
                    LotsMapper.lot2Builder(
                        lots.inactivateLotsList,
                        LotStatus.CLOSED
                    )
                )
            } catch (ex: StatusException) {
                logger.error(ex.message)
            }

            try {
                val rating = reviewCoroutine.await()
                responseBuilder.rating = rating
            } catch (ex: StatusException) {
                logger.error(ex.message)
            }
        }
}