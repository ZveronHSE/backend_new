package ru.zveron.service.api.profile

import com.google.protobuf.Empty
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.dao.DataIntegrityViolationException
import ru.zveron.ChannelType
import ru.zveron.CreateProfileRequest
import ru.zveron.GetProfileRequest
import ru.zveron.GetProfileResponse
import ru.zveron.GetProfileWithContactsRequest
import ru.zveron.GetProfileWithContactsResponse
import ru.zveron.ProfileServiceInternalGrpcKt
import ru.zveron.UpdateContactsRequest
import ru.zveron.mapper.ContactsMapper.linksModel2DTO
import ru.zveron.mapper.ContactsMapper.channelsDTO2Model
import ru.zveron.mapper.ContactsMapper.linksEntity2Model
import ru.zveron.validation.ContactsValidator.validateNumberOfChannels
import ru.zveron.entity.Contact
import ru.zveron.entity.Profile
import ru.zveron.entity.Settings
import ru.zveron.exception.ProfileException
import ru.zveron.getProfileResponse
import ru.zveron.getProfileWithContactsResponse
import ru.zveron.service.ContactService
import ru.zveron.service.ProfileService
import java.time.Instant

@GrpcService
class ProfileServiceInternal(
    private val contactService: ContactService,
    private val profileService: ProfileService,
) : ProfileServiceInternalGrpcKt.ProfileServiceInternalCoroutineImplBase() {

    override suspend fun createProfile(request: CreateProfileRequest): Empty =
        Empty.getDefaultInstance().also {
            val waysOfCommunication = linksModel2DTO(request.links)
            validateNumberOfChannels(waysOfCommunication)
            val profile = Profile(
                id = request.authAccountId,
                name = request.name,
                surname = request.surname,
                imageId = request.imageId,
                lastSeen = Instant.now(),
            )
            profile.contact = Contact(
                profile = profile,
                vkRef = request.links.vk.ref,
                gmail = request.links.gmail.email,
                additionalEmail = request.links.vk.email,
                phone = request.links.phone.number,
            )
            profile.settings = Settings(
                profile = profile,
                channels = waysOfCommunication,
            )

            try {
                profileService.save(profile)
            } catch (e: DataIntegrityViolationException) {
                throw ProfileException("Profile with id: ${request.authAccountId} already exists")
            }
        }

    override suspend fun getProfile(request: GetProfileRequest): GetProfileResponse =
        getProfileResponse {
            val profile = profileService.findByIdOrThrow(request.id)
            id = request.id
            name = profile.name
            surname = profile.surname
            imageId = profile.imageId
            addressId = profile.addressId
            channels.addAll(channelsDTO2Model(profile.settings.channels))
        }


    override suspend fun getProfileWithContacts(request: GetProfileWithContactsRequest): GetProfileWithContactsResponse =
        getProfileWithContactsResponse {
            val profile = profileService.findByIdOrThrow(request.id)
            id = request.id
            name = profile.name
            surname = profile.surname
            imageId = profile.imageId
            addressId = profile.addressId
            channels.addAll(channelsDTO2Model(profile.settings.channels))
            links = linksEntity2Model(profile.contact)
        }

    override suspend fun updateContacts(request: UpdateContactsRequest): Empty =
        Empty.getDefaultInstance().also {
            val contact = contactService.findByIdOrThrow(request.profileId)
            contact.apply {
                when (request.type) {
                    ChannelType.PHONE -> phone = request.links.phone.number
                    ChannelType.VK -> {
                        vkRef = request.links.vk.ref
                        additionalEmail = request.links.vk.email
                    }

                    ChannelType.GOOGLE -> gmail = request.links.gmail.email
                    ChannelType.CHAT -> throw ProfileException("Chat channel type don't need to be added to contacts")
                    else -> throw ProfileException("Unrecognized channel type: ${request.type}")
                }
            }
            contactService.save(contact)
        }
}