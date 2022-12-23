package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.zveron.entity.BlacklistRecord

interface BlacklistRepository : JpaRepository<BlacklistRecord, BlacklistRecord.BlacklistKey> {
    fun existsById_OwnerUserIdAndId_ReportedUserId(ownerUserId: Long, reportedUserId: Long): Boolean

    fun getAllById_OwnerUserId(id: Long): Collection<BlacklistRecord>

    fun deleteAllById_ReportedUserId(id: Long): Long

    fun deleteAllById_OwnerUserId(id: Long): Long
}