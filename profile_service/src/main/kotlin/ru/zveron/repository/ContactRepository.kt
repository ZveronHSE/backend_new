package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.zveron.entity.Contact
import java.util.Optional

interface ContactRepository : JpaRepository<Contact, Long> {

    fun findByPhone(phone: String): Optional<Contact>

    fun findByVkId(vkId: String): Optional<Contact>

    fun findByGmailId(gmailId: String): Optional<Contact>
}