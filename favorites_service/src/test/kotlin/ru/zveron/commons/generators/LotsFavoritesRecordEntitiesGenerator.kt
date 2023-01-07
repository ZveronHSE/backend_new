package ru.zveron.commons.generators

import ru.zveron.entity.LotsFavoritesRecord
import ru.zveron.favorites.lot.addLotToFavoritesRequest
import ru.zveron.favorites.lot.getFavoriteLotsRequest
import ru.zveron.favorites.lot.lotsExistInFavoritesRequest
import ru.zveron.favorites.lot.removeAllByFavoriteLotRequest
import ru.zveron.favorites.lot.removeAllLotsByOwnerRequest
import ru.zveron.favorites.lot.removeLotFromFavoritesRequest

object LotsFavoritesRecordEntitiesGenerator {

    fun generateLotRecords(ownerId: Long, favProfileID: Long) =
        LotsFavoritesRecord(generateKey(ownerId, favProfileID))

    fun generateKey(ownerId: Long, favLotId: Long) = LotsFavoritesRecord.LotsFavoritesKey(
        ownerUserId = ownerId,
        favoriteLotId = favLotId
    )

    fun createAddLotToFavoritesRequest(ownerId: Long, favLotId: Long) =
        addLotToFavoritesRequest {
            favoritesOwnerId = ownerId
            favoriteLotId = favLotId
        }

    fun createRemoveLotFromFavoritesRequest(ownerId: Long, favLotId: Long) =
        removeLotFromFavoritesRequest {
            favoritesOwnerId = ownerId
            favoriteLotId = favLotId
        }

    fun crateLotExistsInFavoritesRequest(ownerId: Long, favLotsId: List<Long>) =
        lotsExistInFavoritesRequest {
            favoritesOwnerId = ownerId
            favoriteLotId.addAll(favLotsId)
        }

    fun createListFavoritesLotsRequest(ownerId: Long) =
        getFavoriteLotsRequest { favoritesOwnerId = ownerId }

    fun createRemoveAllLotsByOwnerRequest(ownerId: Long) =
        removeAllLotsByOwnerRequest { id = ownerId }

    fun createRemoveAllByFavoriteLotRequest(lotId: Long) =
        removeAllByFavoriteLotRequest { id = lotId }
}