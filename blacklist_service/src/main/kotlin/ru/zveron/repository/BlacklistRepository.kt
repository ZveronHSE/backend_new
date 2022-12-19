package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.zveron.entity.BlacklistRecord

@Repository
interface BlacklistRepository : JpaRepository<BlacklistRecord, BlacklistRecord.BlacklistKey> {
    fun existsById_ReportedIdAndId_ReporterId(reportedId: Long, reporterId: Long): Boolean

    fun getById_ReporterId(id: Long): Collection<BlacklistRecord>

    fun deleteAllById_ReportedId(id: Long): Long

    fun deleteAllById_ReporterId(id: Long): Long
}