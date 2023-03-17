package ru.zveron.service.application

import org.springframework.stereotype.Service
import ru.zveron.client.lot.LotClient
import ru.zveron.entity.LotsFavoritesRecord
import ru.zveron.exception.FavoritesException
import ru.zveron.favorites.lot.LotStatus
import ru.zveron.favorites.lot.LotSummary
import ru.zveron.mapper.LotMapper.toFavoritesStatus
import ru.zveron.mapper.LotMapper.toSummary
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

    suspend fun getFavorites(profileId: Long, categoryId: Int): List<LotSummary> {
        val favoriteLotsIds = lotFavoritesComponent.getFavorites(profileId, categoryId).map { it.id.favoriteLotId }
        return lotClient.getLotsById(favoriteLotsIds).lotsList.map { it.toSummary() }
    }

    suspend fun getCounter(lotId: Long) = lotFavoritesComponent.getCounter(lotId)

    suspend fun removeAllByOwner(profileId: Long) = lotFavoritesComponent.removeAllByOwner(profileId)

    suspend fun removeAllByLot(lotId: Long) = lotFavoritesComponent.removeAllByLot(lotId)

    suspend fun removeAllByCategory(profileId: Long, categoryId: Int) =
        lotFavoritesComponent.removeAllByCategoryId(profileId, categoryId)

    suspend fun removeAllByStatus(profileId: Long, status: LotStatus, categoryId: Int) {
        val profileLots = lotFavoritesComponent.getFavorites(profileId, categoryId).map { it.id.favoriteLotId }
        val removedLotsKeys =
            lotClient.getLotsById(profileLots).lotsList.filter { it.status.toFavoritesStatus() == status }
                .map { LotsFavoritesRecord.LotsFavoritesKey(ownerUserId = profileId, favoriteLotId = it.id) }
        lotFavoritesComponent.removeAllByKey(removedLotsKeys)
    }
}