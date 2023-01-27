package ru.zveron.commons.generators

import ru.zveron.entity.ProfilesFavoritesRecord
import ru.zveron.favorites.profile.addProfileToFavoritesRequest
import ru.zveron.favorites.profile.getFavoriteProfilesRequest
import ru.zveron.favorites.profile.profileExistsInFavoritesRequest
import ru.zveron.favorites.profile.removeAllByFavoriteProfileRequest
import ru.zveron.favorites.profile.removeAllProfilesByOwnerRequest
import ru.zveron.favorites.profile.removeProfileFromFavoritesRequest

object ProfilesFavoritesRecordEntitiesGenerator {

    fun generateProfileRecords(ownerId: Long, favProfileID: Long) =
        ProfilesFavoritesRecord(generateKey(ownerId, favProfileID))

    fun generateKey(ownerId: Long, favProfileId: Long) = ProfilesFavoritesRecord.ProfilesFavoritesKey(
        ownerUserId = ownerId,
        favoriteUserId = favProfileId
    )

    fun createAddProfileToFavoritesRequest(ownerId: Long, favProfileId: Long) =
        addProfileToFavoritesRequest {
            favoritesOwnerId = ownerId
            favoriteProfileId = favProfileId
        }

    fun createRemoveProfileFromFavoritesRequest(ownerId: Long, favProfileId: Long) =
        removeProfileFromFavoritesRequest {
            favoritesOwnerId = ownerId
            favoriteProfileId = favProfileId
        }

    fun crateProfileExistsInFavoritesRequest(ownerId: Long, favProfileId: Long) =
        profileExistsInFavoritesRequest {
            favoritesOwnerId = ownerId
            favoriteProfileId = favProfileId
        }

    fun createListFavoritesProfilesRequest(ownerId: Long) =
        getFavoriteProfilesRequest { id = ownerId }

    fun createRemoveAllProfilesByOwnerRequest(ownerId: Long) =
        removeAllProfilesByOwnerRequest { id = ownerId }

    fun createRemoveAllByFavoriteProfileRequest(ownerId: Long) =
        removeAllByFavoriteProfileRequest { id = ownerId }
}
