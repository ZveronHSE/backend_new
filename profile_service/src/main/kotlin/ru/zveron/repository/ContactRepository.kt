package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.zveron.entity.Contact

interface ContactRepository : JpaRepository<Contact, Long>