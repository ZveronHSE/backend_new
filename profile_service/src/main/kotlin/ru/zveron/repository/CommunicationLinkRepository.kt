package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.zveron.entity.CommunicationLink

interface CommunicationLinkRepository : JpaRepository<CommunicationLink, Long> {

    fun findAllByProfileId(id: Long): List<CommunicationLink>

    fun findByCommunicationLinkId(id: String): CommunicationLink?
}