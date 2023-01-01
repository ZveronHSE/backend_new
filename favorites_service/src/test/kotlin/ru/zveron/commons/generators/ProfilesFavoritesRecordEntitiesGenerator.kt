package ru.zveron.commons.generators

import ru.zveron.addProfileToFavoritesRequest
import ru.zveron.entity.ProfilesFavoritesRecord
import ru.zveron.listFavoriteProfilesRequest
import ru.zveron.profileExistsInFavoritesRequest
import ru.zveron.removeAllByFavoriteProfileRequest
import ru.zveron.removeAllProfilesByOwnerRequest
import ru.zveron.removeProfileFromFavoritesRequest

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
        listFavoriteProfilesRequest { favoritesOwnerId = ownerId }

    fun createRemoveAllProfilesByOwnerRequest(ownerId: Long) =
        removeAllProfilesByOwnerRequest { profileId = ownerId }

    fun createRemoveAllByFavoriteProfileRequest(ownerId: Long) =
        removeAllByFavoriteProfileRequest { profileId = ownerId }
}