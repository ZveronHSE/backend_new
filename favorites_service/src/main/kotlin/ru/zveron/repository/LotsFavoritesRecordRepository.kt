package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.zveron.entity.LotsFavoritesRecord
import javax.transaction.Transactional

interface LotsFavoritesRecordRepository: JpaRepository<LotsFavoritesRecord, LotsFavoritesRecord.LotsFavoritesKey> {

    fun countAllById_FavoriteLotId(lotId: Long): Long

    fun getAllById_OwnerUserIdAndCategoryId(ownerUSerId: Long, categoryId: Int): List<LotsFavoritesRecord>

    fun existsById_OwnerUserIdAndId_FavoriteLotId(ownerUserId: Long, favoriteLotId: Long): Boolean

    @Transactional
    fun deleteAllById_OwnerUserId(profileId: Long)

    @Transactional
    fun deleteAllById_FavoriteLotId(lotId: Long)

    @Transactional
    fun deleteById_OwnerUserIdAndCategoryId(profileId: Long, categoryId: Int)
}