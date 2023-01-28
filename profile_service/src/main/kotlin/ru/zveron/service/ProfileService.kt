package ru.zveron.service

import com.google.protobuf.timestamp
import io.grpc.Status
import io.grpc.StatusException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import mu.KLogging
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import ru.zveron.contract.AddressResponse
import ru.zveron.contract.lot.ProfileLotsResponse
import ru.zveron.contract.profile.Contacts
import ru.zveron.contract.profile.CreateProfileRequest
import ru.zveron.contract.profile.GetProfileInfoRequest
import ru.zveron.contract.profile.GetProfileInfoResponse
import ru.zveron.contract.profile.GetProfilePageRequest
import ru.zveron.contract.profile.GetProfilePageResponse
import ru.zveron.contract.profile.GetProfileRequest
import ru.zveron.contract.profile.GetProfileResponse
import ru.zveron.contract.profile.GetProfileWithContactsRequest
import ru.zveron.contract.profile.GetProfileWithContactsResponse
import ru.zveron.contract.profile.LotStatus
import ru.zveron.contract.profile.LotSummary
import ru.zveron.contract.profile.SetProfileInfoRequest
import ru.zveron.contract.profile.contacts
import ru.zveron.contract.profile.getProfileInfoResponse
import ru.zveron.contract.profile.getProfilePageResponse
import ru.zveron.contract.profile.getProfileResponse
import ru.zveron.contract.profile.getProfileWithContactsResponse
import ru.zveron.entity.Contact
import ru.zveron.entity.Profile
import ru.zveron.entity.Settings
import ru.zveron.exception.ProfileException
import ru.zveron.exception.ProfileNotFoundException
import ru.zveron.mapper.AddressMapper.toAddress
import ru.zveron.mapper.AddressMapper.toProfileAddress
import ru.zveron.mapper.AddressMapper.toRequest
import ru.zveron.mapper.ContactsMapper.toDto
import ru.zveron.mapper.ContactsMapper.toModel
import ru.zveron.mapper.LotsMapper.toBuilder
import ru.zveron.repository.ProfileRepository
import ru.zveron.service.client.address.AddressClient
import ru.zveron.service.client.blakclist.BlacklistClient
import ru.zveron.service.client.lot.LotClient
import ru.zveron.service.client.review.ReviewClient
import ru.zveron.validation.ContactsValidator
import java.time.Instant

@Service
class ProfileService(
    private val addressClient: AddressClient,
    private val blacklistClient: BlacklistClient,
    private val lotClient: LotClient,
    private val profileRepository: ProfileRepository,
    private val reviewClient: ReviewClient,
) {

    companion object : KLogging()

    fun deleteById(id: Long) = profileRepository.deleteById(id)

    suspend fun findByIdOrThrow(id: Long): Profile = profileRepository
        .findById(id)
        .orElseThrow { ProfileNotFoundException("Profile with id: $id doesn't exist") }

    suspend fun createProfile(request: CreateProfileRequest) {
        val waysOfCommunication = request.links.toDto()
        ContactsValidator.validateNumberOfChannels(waysOfCommunication)
        ContactsValidator.validateLinks(request.links)
        val profile = Profile(
            id = request.authAccountId,
            name = request.name,
            surname = request.surname,
            imageId = request.imageId,
            lastSeen = Instant.now(),
        )
        profile.contact = Contact(
            profile = profile,
            vkId = request.links.vk.id,
            vkRef = request.links.vk.ref,
            gmailId = request.links.gmail.id,
            gmail = request.links.gmail.email,
            additionalEmail = request.links.vk.email,
            phone = request.links.phone.number,
        )
        profile.settings = Settings(
            profile = profile,
            channels = waysOfCommunication,
        )

        try {
            profileRepository.save(profile)
        } catch (e: DataIntegrityViolationException) {
            throw ProfileException(
                "Profile with id: ${request.authAccountId} already exists",
                Status.INVALID_ARGUMENT.code
            )
        }
    }

    suspend fun getProfilePage(request: GetProfilePageRequest): GetProfilePageResponse = coroutineScope {
        val blacklistCoroutine = inOwnerBlacklist(request.requestedProfileId, request.authorizedProfileId)
        val profile = findByIdOrThrow(request.requestedProfileId)

        val addExtraFields = !blacklistCoroutine.awaitBlacklistResponse()
        val addressCoroutine = getAddressById(profile.addressId, addExtraFields)
        val lotsCoroutine = getLotsBySellerId(profile.id, addExtraFields)
        val ratingCoroutine = getRatingByProfileId(profile.id, addExtraFields)

        getProfilePageResponse {
            id = profile.id
            name = profile.name
            surname = profile.surname
            imageId = profile.imageId
            lastActivity = timestamp { seconds = profile.lastSeen.epochSecond; nanos = profile.lastSeen.nano }
            address = addressCoroutine.awaitAddressResponse().toProfileAddress()
            rating = ratingCoroutine.await()
            contacts = getContacts(profile, addExtraFields)

            val (activeLots, closedLots) = lotsCoroutine.awaitLotsResponse()
            this.activeLots.addAll(activeLots)
            this.closedLots.addAll(closedLots)
        }
    }

    suspend fun getProfileInfo(request: GetProfileInfoRequest): GetProfileInfoResponse = coroutineScope {
        val profile = findByIdOrThrow(request.id)
        val addressCoroutine = getAddressById(profile.addressId)
        val reviewCoroutine = getRatingByProfileId(profile.id)

        getProfileInfoResponse {
            id = profile.id
            name = profile.name
            surname = profile.surname
            imageId = profile.imageId
            address = addressCoroutine.awaitAddressResponse().toAddress()
            rating = reviewCoroutine.awaitRatingResponse()
        }
    }

    suspend fun getProfile(request: GetProfileRequest): GetProfileResponse =
        getProfileResponse {
            val profile = findByIdOrThrow(request.id)
            id = request.id
            name = profile.name
            surname = profile.surname
            imageId = profile.imageId
            addressId = profile.addressId
            channels.addAll(profile.settings.channels.toModel())
        }

    suspend fun getProfileWithContacts(request: GetProfileWithContactsRequest): GetProfileWithContactsResponse =
        getProfileWithContactsResponse {
            val profile = findByIdOrThrow(request.id)
            id = request.id
            name = profile.name
            surname = profile.surname
            imageId = profile.imageId
            addressId = profile.addressId
            channels.addAll(profile.settings.channels.toModel())
            links = profile.contact.toModel()
            lastSeen = timestamp {
                seconds = profile.lastSeen.epochSecond
                nanos = profile.lastSeen.nano
            }
        }

    suspend fun setProfileInfo(request: SetProfileInfoRequest) {
        val profile = findByIdOrThrow(request.id)
        val newAddress = addressClient.saveIfNotExists(request.address.toRequest())
        profile.apply {
            name = request.name
            surname = request.surname
            imageId = request.imageId
            addressId = newAddress.id
        }

        profileRepository.save(profile)
    }

    private fun CoroutineScope.inOwnerBlacklist(
        requestedProfileId: Long,
        authorizedProfileId: Long,
    ): Deferred<Boolean> =
        async(CoroutineName("Exists-In-Blacklist-Coroutine")) {
            if (authorizedProfileId != 0L) blacklistClient.existsInBlacklist(
                ownerId = requestedProfileId,
                targetUserId = authorizedProfileId
            ) else false
        }

    private fun CoroutineScope.getLotsBySellerId(id: Long, condition: Boolean = true): Deferred<ProfileLotsResponse> =
        async(CoroutineName("Get-Lots-Coroutine")) {
            if (condition) lotClient.getLotsBySellerId(id) else ProfileLotsResponse.getDefaultInstance()
        }

    private fun CoroutineScope.getAddressById(id: Long, condition: Boolean = true): Deferred<AddressResponse> =
        async(CoroutineName("Get-Address-Coroutine")) {
            if (condition && id != 0L) addressClient.getById(id) else AddressResponse.getDefaultInstance()
        }

    private fun CoroutineScope.getRatingByProfileId(id: Long, condition: Boolean = true): Deferred<Double> =
        async(CoroutineName("Get-Review-Coroutine")) {
            if (condition) reviewClient.getRating(id) else 0.0
        }

    private suspend fun Deferred<Boolean>.awaitBlacklistResponse(): Boolean =
        try {
            await()
        } catch (ex: StatusException) {
            logger.error(ex.message)
            true
        }

    private suspend fun Deferred<ProfileLotsResponse>.awaitLotsResponse(): Pair<List<LotSummary>, List<LotSummary>> {
        var activeLots = listOf<LotSummary>()
        var closedLots = listOf<LotSummary>()
        try {
            val lots = await()
            activeLots = lots.activateLotsList.toBuilder(LotStatus.ACTIVE)
            closedLots = lots.inactivateLotsList.toBuilder(LotStatus.CLOSED)
        } catch (ex: StatusException) {
            logger.error(ex.message)
        }
        return activeLots to closedLots
    }

    private suspend fun Deferred<AddressResponse>.awaitAddressResponse(): AddressResponse =
        try {
            await()
        } catch (ex: StatusException) {
            logger.error(ex.message)
            AddressResponse.getDefaultInstance()
        }

    private suspend fun Deferred<Double>.awaitRatingResponse(): Double =
        try {
            await()
        } catch (ex: StatusException) {
            logger.error(ex.message)
            0.0
        }

    private fun getContacts(profile: Profile, condition: Boolean) =
        if (condition) contacts {
            links = profile.contact.toModel()
            channels.addAll(profile.settings.channels.toModel())
        } else Contacts.getDefaultInstance()
}