package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import ru.zveron.entity.BlacklistRecord

interface BlacklistRepository : JpaRepository<BlacklistRecord, BlacklistRecord.BlacklistKey> {
    fun existsById_OwnerUserIdAndId_ReportedUserId(ownerUserId: Long, reportedUserId: Long): Boolean

    fun getAllById_OwnerUserId(id: Long): List<BlacklistRecord>

    @Transactional
    fun deleteAllById_ReportedUserId(id: Long): Long

    @Transactional
    fun deleteAllById_OwnerUserId(id: Long): Long
}