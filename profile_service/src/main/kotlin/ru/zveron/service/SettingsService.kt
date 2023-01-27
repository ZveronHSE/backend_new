package ru.zveron.service

import io.grpc.Status
import org.springframework.stereotype.Service
import ru.zveron.contract.profile.GetChannelTypesRequest
import ru.zveron.contract.profile.GetChannelTypesResponse
import ru.zveron.contract.profile.GetSettingsRequest
import ru.zveron.contract.profile.GetSettingsResponse
import ru.zveron.contract.profile.SetSettingsRequest
import ru.zveron.contract.profile.address
import ru.zveron.contract.profile.getChannelTypesResponse
import ru.zveron.contract.profile.getSettingsResponse
import ru.zveron.contract.profile.model.ChannelType
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
    private val repository: SettingsRepository
) {

    private fun findByIdOrThrow(id: Long) =
        repository.findById(id)
            .orElseThrow { ProfileNotFoundException("Profile with id: $id doesn't exist", Status.NOT_FOUND.code) }

    suspend fun getChannelTypes(request: GetChannelTypesRequest): GetChannelTypesResponse =
        getChannelTypesResponse {
            val settings = findByIdOrThrow(request.id)
            channels.addAll(settings.channels.toModel())
        }

    suspend fun getSettings(request: GetSettingsRequest): GetSettingsResponse =
        getSettingsResponse {
            val settings = findByIdOrThrow(request.id)
            val address = addressClient.getById(settings.searchAddressId)
            channels.addAll(settings.channels.toModel())
            this.address = address {
                region = address.region
                town = address.town
                latitude = address.latitude
                longitude = address.longitude
            }
        }

    suspend fun setSettings(request: SetSettingsRequest) {
        val settings = findByIdOrThrow(request.id)
        val links = settings.profile.contact
        if (request.channelsList.contains(ChannelType.VK) && links.vkRef.isBlank()) {
            throw ProfileException(
                "Can't use vk as communication channel because link is missed",
                Status.INVALID_ARGUMENT.code
            )
        }
        if (request.channelsList.contains(ChannelType.GOOGLE) && links.gmail.isBlank()) {
            throw ProfileException(
                "Can't use gmail as communication channel because link is missed",
                Status.INVALID_ARGUMENT.code
            )
        }
        if (request.channelsList.contains(ChannelType.PHONE) && links.phone.isBlank()) {
            throw ProfileException(
                "Can't use phone as communication channel because link is missed",
                Status.INVALID_ARGUMENT.code
            )
        }
        settings.channels = request.channelsList.toSet().toDto()
        ContactsValidator.validateNumberOfChannels(settings.channels)
        ContactsValidator.validateLinksNotBlank(settings.channels, links)
        settings.searchAddressId = addressClient.saveIfNotExists(request.address.toRequest()).id

        repository.save(settings)
    }
}