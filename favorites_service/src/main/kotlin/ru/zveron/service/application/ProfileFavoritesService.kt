package ru.zveron.service.application

import org.springframework.stereotype.Service
import ru.zveron.client.profile.ProfileClient
import ru.zveron.client.rating.ReviewClient
import ru.zveron.exception.FavoritesException
import ru.zveron.favorites.profile.ProfileSummary
import ru.zveron.mapper.ProfileMapper.toFavoritesSummary
import ru.zveron.service.domain.ProfileFavoritesComponent

@Service
class ProfileFavoritesService(
    private val service: ProfileFavoritesComponent,
    private val profileClient: ProfileClient,
    private val reviewClient: ReviewClient,
) {

    suspend fun addToFavorites(authorizedProfileId: Long, targetUserId: Long) {
        if (authorizedProfileId == targetUserId) {
            throw FavoritesException("Нельзя добавить себя в свой список избранного")
        }
        if (!profileClient.existsById(targetUserId)) {
            throw FavoritesException("Пользователя с id: $targetUserId не существует")
        }
        service.addToFavorites(authorizedProfileId = authorizedProfileId, targetUserId = targetUserId)
    }

    suspend fun removeFromFavorites(authorizedProfileId: Long, targetUserId: Long) {
        if (authorizedProfileId == targetUserId) {
            throw FavoritesException("Нельзя удалить себя из своего списка избранного")
        }
        service.removeFromFavorites(authorizedProfileId = authorizedProfileId, targetUserId = targetUserId)
    }

    suspend fun existsInFavorites(favoritesOwnerId: Long, targetUserId: Long): Boolean {
        if (favoritesOwnerId == targetUserId) {
            throw FavoritesException("Профиль не может быть в собственном списке избранного")
        }
        return service.existsInFavorites(favoritesOwnerId = favoritesOwnerId, targetUserId = targetUserId)
    }

    suspend fun getFavoriteProfiles(authorizedProfileId: Long): List<ProfileSummary> {
        val ids = service.getFavoriteProfiles(authorizedProfileId).map { it.id.favoriteUserId }
        return profileClient.getProfilesSummary(ids).map { it.toFavoritesSummary(reviewClient.getProfileRating(it.id)) }
    }

    suspend fun removeAllByOwner(id: Long) = service.removeAllByOwner(id)

    suspend fun removeAllByFavoriteProfile(id: Long) = service.removeAllByFavoriteProfile(id)
}