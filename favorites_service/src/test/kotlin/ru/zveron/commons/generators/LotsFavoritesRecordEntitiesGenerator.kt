package ru.zveron.commons.generators

import ru.zveron.contract.lot.model.Status
import ru.zveron.contract.lot.model.lot
import ru.zveron.entity.LotsFavoritesRecord
import ru.zveron.favorites.lot.addLotToFavoritesRequest
import ru.zveron.favorites.lot.lotsExistInFavoritesRequest
import ru.zveron.favorites.lot.removeAllByFavoriteLotRequest
import ru.zveron.favorites.lot.removeAllLotsByOwnerRequest
import ru.zveron.favorites.lot.removeLotFromFavoritesRequest

object LotsFavoritesRecordEntitiesGenerator {

    fun generateLotRecords(ownerId: Long, favProfileID: Long, categoryId: Int) =
        LotsFavoritesRecord(generateKey(ownerId, favProfileID), categoryId)

    fun generateKey(ownerId: Long, favLotId: Long) = LotsFavoritesRecord.LotsFavoritesKey(
        ownerUserId = ownerId,
        favoriteLotId = favLotId
    )

    fun createAddLotToFavoritesRequest(id: Long) =
        addLotToFavoritesRequest { this.id = id }

    fun createRemoveLotFromFavoritesRequest(id: Long) =
        removeLotFromFavoritesRequest { this.id = id }

    fun crateLotExistsInFavoritesRequest(ownerId: Long, favLotsId: List<Long>) =
        lotsExistInFavoritesRequest {
            favoritesOwnerId = ownerId
            favoriteLotId.addAll(favLotsId)
        }

    fun createRemoveAllLotsByOwnerRequest(ownerId: Long) =
        removeAllLotsByOwnerRequest { id = ownerId }

    fun createRemoveAllByFavoriteLotRequest(lotId: Long) =
        removeAllByFavoriteLotRequest { id = lotId }

    fun generateLot(id: Long, status: Status = Status.ACTIVE) = lot {
        this.id = id
        title = PrimitivesGenerator.generateString(10)
        price = PrimitivesGenerator.generateString(10)
        publicationDate = PrimitivesGenerator.generateString(10)
        this.status = status
        photoId = PrimitivesGenerator.generateUserId()
    }
}