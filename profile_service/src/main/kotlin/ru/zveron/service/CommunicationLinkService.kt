package ru.zveron.service

import io.grpc.Status
import org.springframework.stereotype.Service
import ru.zveron.contract.profile.GetProfileByChannelRequest
import ru.zveron.contract.profile.GetProfileByChannelResponse
import ru.zveron.contract.profile.UpdateContactsRequest
import ru.zveron.contract.profile.VerifyProfileHashRequest
import ru.zveron.contract.profile.getProfileByChannelResponse
import ru.zveron.contract.profile.model.ChannelType
import ru.zveron.domain.link.CommunicationLinkType
import ru.zveron.domain.link.GmailData
import ru.zveron.domain.link.LinksDto
import ru.zveron.domain.link.PhoneData
import ru.zveron.domain.link.VkData
import ru.zveron.domain.profile.ProfileInitializationType
import ru.zveron.entity.CommunicationLink
import ru.zveron.entity.Profile
import ru.zveron.exception.ProfileException
import ru.zveron.exception.ProfileNotFoundException
import ru.zveron.mapper.ContactsMapper.toDto
import ru.zveron.repository.CommunicationLinkRepository

@Service
class CommunicationLinkService(
    private val profileService: ProfileService,
    private val repository: CommunicationLinkRepository
) {

    fun findByIdOrThrow(id: Long): LinksDto =
        repository.findAllByProfileId(id)
            .ifEmpty { throw ProfileNotFoundException("Profile with id: $id doesn't exist") }
            .toDto()

    suspend fun updateContacts(request: UpdateContactsRequest) {
        val profile = profileService.findByIdOrThrow(request.profileId, ProfileInitializationType.COMMUNICATION_LINKS)
        val linksDto = profile.communicationLinks.toDto()
        val updatedLink = when (request.type) {
            ChannelType.PHONE -> createOrUpdatePhone(linksDto, request.links.phone.number, profile)
            ChannelType.VK -> createOrUpdateVk(
                linksDto,
                request.links.vk.id,
                request.links.vk.ref,
                request.links.vk.email,
                profile
            )

            ChannelType.GOOGLE -> createOrUpdateGmail(
                linksDto,
                request.links.gmail.id,
                request.links.gmail.email,
                profile,
            )

            ChannelType.CHAT -> throw ProfileException(
                "Chat channel type don't need to be added to contacts",
                Status.INVALID_ARGUMENT.code
            )

            else -> throw ProfileException(
                "Unrecognized channel type: ${request.type}",
                Status.INVALID_ARGUMENT.code
            )
        }
        repository.save(updatedLink)
    }

    suspend fun getProfileByChannel(request: GetProfileByChannelRequest): GetProfileByChannelResponse =
        getProfileByChannelResponse {
            val profile = findByChannelOrThrow(request.type, request.identifier).profile
            id = profile.id
            name = profile.name
            surname = profile.surname
        }

    suspend fun isPasswordHashValid(request: VerifyProfileHashRequest): Boolean {
        val link = repository.findByCommunicationLinkIdAndType(request.phoneNumber, CommunicationLinkType.PHONE)
            ?: return false
        return (link.data as PhoneData).passwordHash == request.passwordHash
    }

    private fun createOrUpdatePhone(
        links: LinksDto,
        phoneNumber: String,
        profile: Profile,
    ): CommunicationLink {
        val link = links.phoneLink ?: return CommunicationLink(
            communicationLinkId = phoneNumber,
            data = PhoneData(),
            profile = profile,
        )

        return link.copy(communicationLinkId = phoneNumber)
    }

    private fun createOrUpdateVk(
        links: LinksDto,
        id: String,
        ref: String,
        email: String,
        profile: Profile,
    ): CommunicationLink {
        val data = VkData(
            ref = ref,
            email = email,
        )
        val link = links.vkLink ?: return CommunicationLink(
            communicationLinkId = id,
            data = data,
            profile = profile,
        )

        return link.copy(communicationLinkId = id, data = data)
    }

    private fun createOrUpdateGmail(
        links: LinksDto,
        id: String,
        email: String,
        profile: Profile,
    ): CommunicationLink {
        val data = GmailData(
            email = email,
        )
        val link = links.gmailLink ?: return CommunicationLink(
            communicationLinkId = id,
            data = data,
            profile = profile,
        )

        return link.copy(communicationLinkId = id, data = data)
    }

    private fun findByChannelOrThrow(channelType: ChannelType, id: String): CommunicationLink = when (channelType) {
        ChannelType.PHONE -> repository.findByCommunicationLinkIdAndType(id, CommunicationLinkType.PHONE)
        ChannelType.GOOGLE -> repository.findByCommunicationLinkIdAndType(id, CommunicationLinkType.GMAIL)
        ChannelType.VK -> repository.findByCommunicationLinkIdAndType(id, CommunicationLinkType.VK)
        else -> throw ProfileException("Profile can't be find by channel $channelType", Status.INVALID_ARGUMENT.code)
    } ?: throw ProfileNotFoundException("Can't find profile by channel: $channelType and channel id: $id")
}