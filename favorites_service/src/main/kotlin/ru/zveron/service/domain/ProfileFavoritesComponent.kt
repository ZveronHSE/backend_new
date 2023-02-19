package ru.zveron.service.domain

import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Service
import ru.zveron.entity.ProfilesFavoritesRecord
import ru.zveron.exception.FavoritesException
import ru.zveron.repository.ProfilesFavoritesRecordRepository

@Service
class ProfileFavoritesComponent(private val repository: ProfilesFavoritesRecordRepository) {

    suspend fun addToFavorites(authorizedProfileId: Long, targetUserId: Long) = repository.save(
        ProfilesFavoritesRecord(
            ProfilesFavoritesRecord.ProfilesFavoritesKey(
                ownerUserId = authorizedProfileId,
                favoriteUserId = targetUserId,
            )
        )
    )

    /**
     * @throws FavoritesException если удаляемый профиль не находится в избранном
     */
    suspend fun removeFromFavorites(authorizedProfileId: Long, targetUserId: Long) = try {
        repository.deleteById(
            ProfilesFavoritesRecord.ProfilesFavoritesKey(
                ownerUserId = authorizedProfileId,
                favoriteUserId = targetUserId,
            )
        )
    } catch (e: EmptyResultDataAccessException) {
        throw FavoritesException("Нельзя удалить профиль не из списка избранного")
    }

    suspend fun existsInFavorites(favoritesOwnerId: Long, targetUserId: Long) = repository.existsById(
        ProfilesFavoritesRecord.ProfilesFavoritesKey(
            ownerUserId = favoritesOwnerId,
            favoriteUserId = targetUserId,
        )
    )

    suspend fun getFavoriteProfiles(authorizedProfileId: Long) =
        repository.getAllById_OwnerUserId(authorizedProfileId)

    suspend fun removeAllByOwner(id: Long) = repository.deleteAllById_OwnerUserId(id)

    suspend fun removeAllByFavoriteProfile(id: Long) = repository.deleteAllById_FavoriteUserId(id)
}