package ru.zveron.commons.generators

import ru.zveron.contract.profile.profileSummary
import ru.zveron.entity.ProfilesFavoritesRecord
import ru.zveron.favorites.profile.addProfileToFavoritesRequest
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

    fun createAddProfileToFavoritesRequest(id:  Long) =
        addProfileToFavoritesRequest {
            this.id = id
        }

    fun createRemoveProfileFromFavoritesRequest(id: Long) =
        removeProfileFromFavoritesRequest { this.id = id }

    fun crateProfileExistsInFavoritesRequest(ownerId: Long, favProfileId: Long) =
        profileExistsInFavoritesRequest {
            favoritesOwnerId = ownerId
            favoriteProfileId = favProfileId
        }

    fun createRemoveAllProfilesByOwnerRequest(ownerId: Long) =
        removeAllProfilesByOwnerRequest { id = ownerId }

    fun createRemoveAllByFavoriteProfileRequest(ownerId: Long) =
        removeAllByFavoriteProfileRequest { id = ownerId }

    fun generateProfileSummary(id: Long) = profileSummary {
        this.id = id
        name = PrimitivesGenerator.generateString(10)
        surname = PrimitivesGenerator.generateString(10)
        imageUrl = PrimitivesGenerator.generateString(10)
        addressId = PrimitivesGenerator.generateUserId()
    }
}