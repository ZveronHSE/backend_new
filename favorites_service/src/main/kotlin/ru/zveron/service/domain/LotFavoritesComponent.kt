package ru.zveron.service.domain

import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Service
import ru.zveron.entity.LotsFavoritesRecord
import ru.zveron.exception.FavoritesException
import ru.zveron.repository.LotsFavoritesRecordRepository

@Service
class LotFavoritesComponent(private val lotRepository: LotsFavoritesRecordRepository) {

    suspend fun addToFavorites(profileId: Long, lotId: Long, categoryId: Int) = lotRepository.save(
        LotsFavoritesRecord(
            LotsFavoritesRecord.LotsFavoritesKey(
                ownerUserId = profileId,
                favoriteLotId = lotId
            ),
            categoryId
        )
    )

    /**
     * @throws FavoritesException если удаляемое объявление не находится в избранном
     */
    suspend fun removeFromFavorites(profileId: Long, lotId: Long) = try {
        lotRepository.deleteById(
            LotsFavoritesRecord.LotsFavoritesKey(
                ownerUserId = profileId,
                favoriteLotId = lotId
            )
        )
    } catch (e: EmptyResultDataAccessException) {
        throw FavoritesException("Нельзя удалить объявление не из списка избранного")
    }

    suspend fun existsInFavorites(profileId: Long, lotIds: List<Long>) = lotIds.map { id ->
        lotRepository.existsById_OwnerUserIdAndId_FavoriteLotId(
            ownerUserId = profileId,
            favoriteLotId = id
        )
    }

    suspend fun getFavorites(profileId: Long, categoryId: Int) =
        lotRepository.getAllById_OwnerUserIdAndCategoryId(profileId, categoryId)

    suspend fun getCounter(lotId: Long) = lotRepository.countAllById_FavoriteLotId(lotId)

    suspend fun removeAllByOwner(profileId: Long) = lotRepository.deleteAllById_OwnerUserId(profileId)

    suspend fun removeAllByLot(lotId: Long) = lotRepository.deleteAllById_FavoriteLotId(lotId)

    suspend fun removeAllByCategoryId(profileId: Long, categoryId: Int) =
        lotRepository.deleteById_OwnerUserIdAndCategoryId(profileId, categoryId)

    suspend fun removeAllByKey(keys: List<LotsFavoritesRecord.LotsFavoritesKey>) = lotRepository.deleteAllById(keys)
}