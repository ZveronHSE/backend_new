package ru.zveron.service

import com.google.protobuf.timestamp
import io.grpc.Status
import io.grpc.StatusException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
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
import ru.zveron.contract.profile.SetProfileInfoRequest
import ru.zveron.contract.profile.contacts
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
import ru.zveron.service.api.ProfileServiceExternal
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

    fun deleteById(id: Long) = profileRepository.deleteById(id)

    suspend fun findByIdOrThrow(id: Long): Profile = profileRepository
        .findById(id)
        .orElseThrow { ProfileNotFoundException("Profile with id: $id doesn't exist", Status.NOT_FOUND.code) }

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


    suspend fun getProfilePage(request: GetProfilePageRequest): GetProfilePageResponse {
        val profile = findByIdOrThrow(request.requestedProfileId)
        val responseBuilder = GetProfilePageResponse.newBuilder().apply {
            id = profile.id
            name = profile.name
            surname = profile.surname
            imageId = profile.imageId
            lastActivity = timestamp { seconds = profile.lastSeen.epochSecond; nanos = profile.lastSeen.nano }
        }

        if (request.authorizedProfileId == 0L || !blacklistClient.existsInBlacklist(
                ownerId = request.requestedProfileId,
                targetUserId = request.authorizedProfileId
            )
        ) {
            addAdditionalFields(profile, responseBuilder)
        }

        return responseBuilder.build()
    }

    suspend fun getProfileInfo(request: GetProfileInfoRequest): GetProfileInfoResponse {
        val profile = findByIdOrThrow(request.id)
        val response = GetProfileInfoResponse.newBuilder().apply {
            id = profile.id
            name = profile.name
            surname = profile.surname
            imageId = profile.imageId
        }

        coroutineScope {
            val addressCoroutine = async(CoroutineName("Get-Address-Coroutine")) {
                if (profile.addressId != 0L) addressClient.getById(profile.addressId) else null
            }
            val reviewCoroutine = async(CoroutineName("Get-Review-Coroutine")) {
                reviewClient.getRating(profile.id)
            }
            try {
                response.rating = reviewCoroutine.await()
            } catch (ex: StatusException) {
                ProfileServiceExternal.logger.error(ex.message)
            }
            try {
                addressCoroutine.await()?.let { response.address = it.toAddress() }
            } catch (ex: StatusException) {
                ProfileServiceExternal.logger.error(ex.message)
            }
        }

        return response.build()
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

    private suspend fun addAdditionalFields(profile: Profile, responseBuilder: GetProfilePageResponse.Builder) =
        coroutineScope {
            val lotsCoroutine = async(CoroutineName("Get-Lots-Coroutine")) { lotClient.getLotsBySellerId(profile.id) }
            val addressCoroutine = async(CoroutineName("Get-Address-Coroutine")) {
                if (profile.addressId != 0L) addressClient.getById(profile.addressId) else null
            }
            val reviewCoroutine = async(CoroutineName("Get-Review-Coroutine")) {
                reviewClient.getRating(profile.id)
            }

            responseBuilder.contacts = contacts {
                links = profile.contact.toModel()
                channels.addAll(profile.settings.channels.toModel())
            }

            try {
                val address = addressCoroutine.await()
                responseBuilder.address = address?.toProfileAddress()
            } catch (ex: StatusException) {
                ProfileServiceExternal.logger.error(ex.message)
            }

            try {
                val lots = lotsCoroutine.await()
                responseBuilder.addAllActiveLots(
                    lots.activateLotsList.toBuilder(
                        LotStatus.ACTIVE
                    )
                )
                responseBuilder.addAllClosedLots(
                    lots.inactivateLotsList.toBuilder(
                        LotStatus.CLOSED
                    )
                )
            } catch (ex: StatusException) {
                ProfileServiceExternal.logger.error(ex.message)
            }

            try {
                val rating = reviewCoroutine.await()
                responseBuilder.rating = rating
            } catch (ex: StatusException) {
                ProfileServiceExternal.logger.error(ex.message)
            }
        }
}