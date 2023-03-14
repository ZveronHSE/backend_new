package ru.zveron.service.application

import org.springframework.stereotype.Service
import ru.zveron.client.lot.LotClient
import ru.zveron.contract.core.Lot
import ru.zveron.contract.core.Status
import ru.zveron.entity.LotsFavoritesRecord
import ru.zveron.exception.FavoritesException
import ru.zveron.service.domain.LotFavoritesComponent

@Service
class LotFavoritesService(
    private val lotFavoritesComponent: LotFavoritesComponent,
    private val lotClient: LotClient
) {

    suspend fun addToFavorites(profileId: Long, lotId: Long): LotsFavoritesRecord {
        val lots = lotClient.getLotsById(listOf(lotId)).lotsList
        if (lots.isEmpty()) {
            throw FavoritesException("Объявления с id: $lotId не существует")
        }
        return lotFavoritesComponent.addToFavorites(profileId = profileId, lotId = lotId, lots.first().categoryId)
    }

    suspend fun removeFromFavorites(profileId: Long, lotId: Long) =
        lotFavoritesComponent.removeFromFavorites(profileId = profileId, lotId = lotId)

    suspend fun existsInFavorites(profileId: Long, lotIds: List<Long>) =
        lotFavoritesComponent.existsInFavorites(profileId = profileId, lotIds = lotIds)

    suspend fun getFavorites(profileId: Long, categoryId: Int): List<Lot> {
        val favoriteLotsIds = lotFavoritesComponent.getFavorites(profileId, categoryId).map { it.id.favoriteLotId }
        return lotClient.getLotsById(favoriteLotsIds).lotsList
    }

    suspend fun getCounter(lotId: Long) = lotFavoritesComponent.getCounter(lotId)

    suspend fun removeAllByOwner(profileId: Long) = lotFavoritesComponent.removeAllByOwner(profileId)

    suspend fun removeAllByLot(lotId: Long) = lotFavoritesComponent.removeAllByLot(lotId)

    suspend fun removeAllByCategory(profileId: Long, categoryId: Int) =
        lotFavoritesComponent.removeAllByCategoryId(profileId, categoryId)

    suspend fun removeAllByStatus(profileId: Long, status: Status, categoryId: Int) {
        val profileLots = lotFavoritesComponent.getFavorites(profileId, categoryId).map { it.id.favoriteLotId }
        val removedLotsKeys =
            lotClient.getLotsById(profileLots).lotsList.filter { it.status == status }
                .map { LotsFavoritesRecord.LotsFavoritesKey(ownerUserId = profileId, favoriteLotId = it.id) }
        lotFavoritesComponent.removeAllByKey(removedLotsKeys)
    }
}