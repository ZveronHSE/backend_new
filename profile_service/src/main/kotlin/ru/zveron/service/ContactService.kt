package ru.zveron.service

import org.springframework.stereotype.Service
import ru.zveron.ChannelType
import ru.zveron.entity.Contact
import ru.zveron.exception.ProfileException
import ru.zveron.exception.ProfileNotFoundException
import ru.zveron.repository.ContactRepository

@Service
class ContactService(private val repository: ContactRepository) {

    fun findByIdOrThrow(id: Long): Contact =
        repository.findById(id).orElseThrow { ProfileNotFoundException("Profile with id: $id doesn't exist") }

    fun save(contact: Contact) = repository.save(contact)

    fun findByChannelOrThrow(channelType: ChannelType, id: String): Contact = when(channelType){
        ChannelType.PHONE -> repository.findByPhone(id)
        ChannelType.GOOGLE -> repository.findByGmailId(id)
        ChannelType.VK -> repository.findByVkId(id)
        else -> throw ProfileException("Profile can't be find by channel $channelType")
    }.orElseThrow { ProfileNotFoundException("Can't find profile by channel: $channelType and channel id: $id") }
}