package ru.zveron.commons.generators

import ru.zveron.addLotToFavoritesRequest
import ru.zveron.entity.LotsFavoritesRecord
import ru.zveron.listFavoriteLotsRequest
import ru.zveron.lotExistsInFavoritesRequest
import ru.zveron.removeAllByFavoriteLotRequest
import ru.zveron.removeAllLotsByOwnerRequest
import ru.zveron.removeLotFromFavoritesRequest

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

    fun crateLotExistsInFavoritesRequest(ownerId: Long, favLotId: Long) =
        lotExistsInFavoritesRequest {
            favoritesOwnerId = ownerId
            favoriteLotId = favLotId
        }

    fun createListFavoritesLotsRequest(ownerId: Long) =
        listFavoriteLotsRequest { favoritesOwnerId = ownerId }

    fun createRemoveAllLotsByOwnerRequest(ownerId: Long) =
        removeAllLotsByOwnerRequest { profileId = ownerId }

    fun createRemoveAllByFavoriteLotRequest(lotId: Long) =
        removeAllByFavoriteLotRequest { this.lotId = lotId }
}