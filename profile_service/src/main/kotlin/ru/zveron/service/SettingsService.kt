package ru.zveron.service

import io.grpc.Status
import org.springframework.stereotype.Service
import ru.zveron.contract.profile.GetChannelTypesResponse
import ru.zveron.contract.profile.GetSettingsResponse
import ru.zveron.contract.profile.SetSettingsRequest
import ru.zveron.contract.profile.address
import ru.zveron.contract.profile.getChannelTypesResponse
import ru.zveron.contract.profile.getSettingsResponse
import ru.zveron.contract.profile.model.ChannelType
import ru.zveron.domain.profile.ProfileInitializationType
import ru.zveron.exception.ProfileException
import ru.zveron.exception.ProfileNotFoundException
import ru.zveron.mapper.AddressMapper.toRequest
import ru.zveron.mapper.ContactsMapper.toDto
import ru.zveron.mapper.ContactsMapper.toModel
import ru.zveron.repository.SettingsRepository
import ru.zveron.service.client.address.AddressClient
import ru.zveron.validation.ContactsValidator

@Service
class SettingsService(
    private val addressClient: AddressClient,
    private val profileService: ProfileService,
    private val repository: SettingsRepository,
) {

    suspend fun getChannelTypes(id: Long): GetChannelTypesResponse =
        getChannelTypesResponse {
            val settings = findByIdOrThrow(id)
            channels.addAll(settings.channels.toModel())
        }

    suspend fun getSettings(authorizedProfileId: Long): GetSettingsResponse =
        getSettingsResponse {
            val settings = findByIdOrThrow(authorizedProfileId)
            val address = addressClient.getById(settings.searchAddressId)
            channels.addAll(settings.channels.toModel())
            this.address = address {
                region = address.region
                town = address.town
                latitude = address.latitude
                longitude = address.longitude
            }
        }

    suspend fun setSettings(request: SetSettingsRequest, authorizedProfileId: Long) {
        val profile = profileService.findByIdOrThrow(authorizedProfileId, ProfileInitializationType.COMMUNICATION_LINKS)
        val settings = profile.settings
        val links = profile.communicationLinks.toDto()
        val channels = request.channelsList.toSet().toDto()
        if (request.channelsList.contains(ChannelType.VK) && links.vkLink == null) {
            throw ProfileException(
                "Can't use vk as communication channel because link is missed",
                Status.INVALID_ARGUMENT.code
            )
        }
        if (request.channelsList.contains(ChannelType.GOOGLE) && links.gmailLink == null) {
            throw ProfileException(
                "Can't use gmail as communication channel because link is missed",
                Status.INVALID_ARGUMENT.code
            )
        }
        if (request.channelsList.contains(ChannelType.PHONE) && links.phoneLink == null) {
            throw ProfileException(
                "Can't use phone as communication channel because link is missed",
                Status.INVALID_ARGUMENT.code
            )
        }
        ContactsValidator.validateNumberOfChannels(channels)
        ContactsValidator.validateLinksNotBlank(channels, links)

        repository.save(
            settings.copy(
                channels = channels,
                searchAddressId = addressClient.saveIfNotExists(request.address.toRequest()).id,
            )
        )
    }

    private fun findByIdOrThrow(id: Long) =
        repository.findById(id)
            .orElseThrow { ProfileNotFoundException("Profile with id: $id doesn't exist") }
}