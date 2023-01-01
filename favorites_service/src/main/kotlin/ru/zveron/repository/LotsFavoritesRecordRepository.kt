package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.zveron.entity.LotsFavoritesRecord
import javax.transaction.Transactional

interface LotsFavoritesRecordRepository: JpaRepository<LotsFavoritesRecord, LotsFavoritesRecord.LotsFavoritesKey> {

    fun getAllById_OwnerUserId(ownerUSerId: Long): List<LotsFavoritesRecord>

    @Transactional
    fun deleteAllById_OwnerUserId(profileId: Long)

    @Transactional
    fun deleteAllById_FavoriteLotId(lotId: Long)
}