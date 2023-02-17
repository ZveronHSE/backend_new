package ru.zveron.service.application

import org.springframework.stereotype.Service
import ru.zveron.client.lot.LotClient
import ru.zveron.favorites.lot.LotSummary
import ru.zveron.mapper.LotMapper.toSummary
import ru.zveron.service.domain.LotFavoritesComponent

@Service
class LotFavoritesService(
    private val lotFavoritesComponent: LotFavoritesComponent,
    private val lotClient: LotClient
) {

    suspend fun addToFavorites(profileId: Long, lotId: Long) =
        lotFavoritesComponent.addToFavorites(profileId = profileId, lotId = lotId)

    suspend fun removeFromFavorites(profileId: Long, lotId: Long) =
        lotFavoritesComponent.removeFromFavorites(profileId = profileId, lotId = lotId)

    suspend fun existsInFavorites(profileId: Long, lotIds: List<Long>) =
        lotFavoritesComponent.existsInFavorites(profileId = profileId, lotIds = lotIds)

    suspend fun getFavorites(profileId: Long): List<LotSummary> {
        val favoriteLotsIds = lotFavoritesComponent.getFavorites(profileId).map { it.id.favoriteLotId }
        return lotClient.getLotsById(favoriteLotsIds).lotsList.map { it.toSummary() }
    }

    suspend fun getCounter(lotId: Long) = lotFavoritesComponent.getCounter(lotId)

    suspend fun removeAllByOwner(profileId: Long) = lotFavoritesComponent.removeAllByOwner(profileId)

    suspend fun removeAllByLot(lotId: Long) = lotFavoritesComponent.removeAllByLot(lotId)
}