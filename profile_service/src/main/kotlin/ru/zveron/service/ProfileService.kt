package ru.zveron.service

import com.google.protobuf.timestamp
import io.grpc.Status
import io.grpc.StatusException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import mu.KLogging
import org.hibernate.exception.ConstraintViolationException
import org.hibernate.jpa.QueryHints
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import ru.zveron.contract.AddressResponse
import ru.zveron.contract.lot.ProfileLotsResponse
import ru.zveron.contract.profile.Contacts
import ru.zveron.contract.profile.CreateProfileRequest
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
import ru.zveron.domain.profile.ProfileInitializationType
import ru.zveron.entity.Profile
import ru.zveron.entity.Settings
import ru.zveron.exception.ProfileNotFoundException
import ru.zveron.exception.ProfileException
import ru.zveron.mapper.AddressMapper.toAddress
import ru.zveron.mapper.AddressMapper.toProfileAddress
import ru.zveron.mapper.AddressMapper.toRequest
import ru.zveron.mapper.ContactsMapper.toCommunicationLinks
import ru.zveron.mapper.ContactsMapper.toDto
import ru.zveron.mapper.ContactsMapper.toModel
import ru.zveron.mapper.ContactsMapper.toLinks
import ru.zveron.mapper.LotsMapper.toBuilder
import ru.zveron.repository.ProfileRepository
import ru.zveron.service.client.address.AddressClient
import ru.zveron.service.client.blakclist.BlacklistClient
import ru.zveron.service.client.lot.LotClient
import ru.zveron.service.client.review.ReviewClient
import ru.zveron.validation.ContactsValidator
import java.time.Instant
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Service
class ProfileService(
    private val addressClient: AddressClient,
    private val blacklistClient: BlacklistClient,
    @PersistenceContext
    private val entityManager: EntityManager,
    private val lotClient: LotClient,
    private val profileRepository: ProfileRepository,
    private val reviewClient: ReviewClient,
) {

    companion object : KLogging()

    fun deleteById(id: Long) = profileRepository.deleteById(id)

    suspend fun findByIdOrThrow(id: Long, initType: ProfileInitializationType): Profile {
        val properties = initType.graphName?.let {
            mapOf(QueryHints.HINT_LOADGRAPH to entityManager.getEntityGraph(it))
        } ?: emptyMap()

        return entityManager.find(Profile::class.java, id, properties)
            ?: throw ProfileNotFoundException("Profile with id: $id doesn't exist")
    }

    suspend fun createProfile(request: CreateProfileRequest): Long {
        val waysOfCommunication = request.links.toDto()
        ContactsValidator.validateNumberOfChannels(waysOfCommunication)
        ContactsValidator.validateLinks(request.links)
        val profile = Profile(
            name = request.name,
            surname = request.surname,
            imageId = request.imageId,
            lastSeen = Instant.now(),
        )
        profile.communicationLinks.addAll(request.links.toCommunicationLinks(profile, request.passwordHash))
        profile.settings = Settings(
            profile = profile,
            channels = waysOfCommunication,
        )

        try {
            return profileRepository.save(profile).id
        } catch (e: DataIntegrityViolationException) {
            if (e.cause is ConstraintViolationException) {
                throw ProfileException(
                    "Specified communication link is already used",
                    Status.INVALID_ARGUMENT.code
                )
            }
            throw e
        }
    }

    suspend fun getProfilePage(request: GetProfilePageRequest, authorizedProfileId: Long): GetProfilePageResponse =
        supervisorScope {
            val blacklistCoroutine = inOwnerBlacklist(request.requestedProfileId, authorizedProfileId)
            val profile = findByIdOrThrow(request.requestedProfileId, ProfileInitializationType.COMMUNICATION_LINKS)

            val addExtraFields = !blacklistCoroutine.awaitBlacklistResponse()
            val addressCoroutine = getAddressById(profile.addressId, addExtraFields)
            val lotsCoroutine = getLotsBySellerId(profile.id, authorizedProfileId, addExtraFields)
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

    suspend fun getProfileInfo(authorizedProfileId: Long): GetProfileInfoResponse = supervisorScope {
        val profile = findByIdOrThrow(authorizedProfileId, ProfileInitializationType.DEFAULT)
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
            val profile = findByIdOrThrow(request.id, ProfileInitializationType.DEFAULT)
            id = request.id
            name = profile.name
            surname = profile.surname
            imageId = profile.imageId
            addressId = profile.addressId
            channels.addAll(profile.settings.channels.toModel())
        }

    suspend fun getProfileWithContacts(request: GetProfileWithContactsRequest): GetProfileWithContactsResponse =
        getProfileWithContactsResponse {
            val profile = findByIdOrThrow(request.id, ProfileInitializationType.COMMUNICATION_LINKS)
            id = request.id
            name = profile.name
            surname = profile.surname
            imageId = profile.imageId
            addressId = profile.addressId
            channels.addAll(profile.settings.channels.toModel())
            links = profile.communicationLinks.toDto().toLinks()
            lastSeen = timestamp {
                seconds = profile.lastSeen.epochSecond
                nanos = profile.lastSeen.nano
            }
        }

    suspend fun setProfileInfo(request: SetProfileInfoRequest, authorizedProfileId: Long) {
        val profile = findByIdOrThrow(authorizedProfileId, ProfileInitializationType.DEFAULT)
        val newAddress = addressClient.saveIfNotExists(request.address.toRequest())
        val updatedProfile = profile.copy(
            name = request.name,
            surname = request.surname,
            imageId = request.imageId,
            addressId = newAddress.id,
        )

        profileRepository.save(updatedProfile)
    }

    private fun CoroutineScope.inOwnerBlacklist(
        requestedProfileId: Long,
        authorizedProfileId: Long,
    ): Deferred<Boolean> =
        async(CoroutineName("Exists-In-Blacklist-Coroutine")) {
            if (authorizedProfileId != 0L && requestedProfileId != authorizedProfileId)
                blacklistClient.existsInBlacklist(
                    ownerId = requestedProfileId,
                    targetUserId = authorizedProfileId
                ) else false
        }

    private fun CoroutineScope.getLotsBySellerId(sellerId: Long, userId: Long, condition: Boolean = true): Deferred<ProfileLotsResponse> =
        async(CoroutineName("Get-Lots-Coroutine")) {
            if (condition) lotClient.getLotsBySellerId(sellerId, userId) else ProfileLotsResponse.getDefaultInstance()
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
            links = profile.communicationLinks.toDto().toLinks()
            channels.addAll(profile.settings.channels.toModel())
        } else Contacts.getDefaultInstance()
}