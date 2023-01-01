package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.zveron.entity.ProfilesFavoritesRecord
import javax.transaction.Transactional

interface ProfilesFavoritesRecordRepository: JpaRepository<ProfilesFavoritesRecord, ProfilesFavoritesRecord.ProfilesFavoritesKey> {

    fun getAllById_OwnerUserId(ownerUserId: Long): List<ProfilesFavoritesRecord>

    @Transactional
    fun deleteAllById_OwnerUserId(profileId: Long)

    @Transactional
    fun deleteAllById_FavoriteUserId(profileId: Long)
}