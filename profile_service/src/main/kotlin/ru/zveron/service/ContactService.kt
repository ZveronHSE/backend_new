package ru.zveron.service

import io.grpc.Status
import org.springframework.stereotype.Service
import ru.zveron.contract.profile.GetProfileByChannelRequest
import ru.zveron.contract.profile.GetProfileByChannelResponse
import ru.zveron.contract.profile.UpdateContactsRequest
import ru.zveron.contract.profile.getProfileByChannelResponse
import ru.zveron.contract.profile.model.ChannelType
import ru.zveron.entity.Contact
import ru.zveron.exception.ProfileException
import ru.zveron.exception.ProfileNotFoundException
import ru.zveron.repository.ContactRepository

@Service
class ContactService(private val repository: ContactRepository) {

    fun findByIdOrThrow(id: Long): Contact =
        repository.findById(id)
            .orElseThrow { ProfileNotFoundException("Profile with id: $id doesn't exist") }

    suspend fun updateContacts(request: UpdateContactsRequest) {
        val contact = findByIdOrThrow(request.profileId)
        val updatedContacts = when (request.type) {
            ChannelType.PHONE -> contact.copy(phone = request.links.phone.number)
            ChannelType.VK -> {
                contact.copy(
                    vkId = request.links.vk.id,
                    vkRef = request.links.vk.ref,
                    additionalEmail = request.links.vk.email,
                )
            }

            ChannelType.GOOGLE -> {
                contact.copy(
                    gmailId = request.links.gmail.id,
                    gmail = request.links.gmail.email,
                )
            }

            ChannelType.CHAT -> throw ProfileException(
                "Chat channel type don't need to be added to contacts",
                Status.INVALID_ARGUMENT.code
            )

            else -> throw ProfileException(
                "Unrecognized channel type: ${request.type}",
                Status.INVALID_ARGUMENT.code
            )
        }
        repository.save(updatedContacts)
    }

    suspend fun getProfileByChannel(request: GetProfileByChannelRequest): GetProfileByChannelResponse =
        getProfileByChannelResponse {
            val profile = findByChannelOrThrow(request.type, request.identifier).profile
            id = profile.id
            name = profile.name
            surname = profile.surname
        }

    private fun findByChannelOrThrow(channelType: ChannelType, id: String): Contact = when (channelType) {
        ChannelType.PHONE -> repository.findByPhone(id)
        ChannelType.GOOGLE -> repository.findByGmailId(id)
        ChannelType.VK -> repository.findByVkId(id)
        else -> throw ProfileException("Profile can't be find by channel $channelType", Status.INVALID_ARGUMENT.code)
    } ?: throw ProfileNotFoundException("Can't find profile by channel: $channelType and channel id: $id")
}