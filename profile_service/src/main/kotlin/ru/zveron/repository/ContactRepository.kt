package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.zveron.entity.Contact

interface ContactRepository : JpaRepository<Contact, Long> {

    fun findByPhone(phone: String): Contact?

    fun findByVkId(vkId: String): Contact?

    fun findByGmailId(gmailId: String): Contact?
}