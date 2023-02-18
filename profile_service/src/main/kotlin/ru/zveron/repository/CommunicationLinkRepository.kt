package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.zveron.domain.link.CommunicationLinkType
import ru.zveron.entity.CommunicationLink

interface CommunicationLinkRepository : JpaRepository<CommunicationLink, Long> {

    fun findAllByProfileId(id: Long): List<CommunicationLink>

    fun findByCommunicationLinkIdAndType(id: String, type: CommunicationLinkType): CommunicationLink?
}