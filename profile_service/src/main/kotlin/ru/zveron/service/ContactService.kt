package ru.zveron.service

import org.springframework.stereotype.Service
import ru.zveron.entity.Contact
import ru.zveron.exception.ProfileException
import ru.zveron.repository.ContactRepository

@Service
class ContactService(private val repository: ContactRepository) {

    fun findByIdOrThrow(id: Long): Contact =
        repository.findById(id).orElseThrow { ProfileException("Profile with id: $id doesn't exist") }

    fun save(contact: Contact) = repository.save(contact)
}