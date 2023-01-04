package ru.zveron.service

import com.google.protobuf.Empty
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Service
import ru.zveron.entity.LotsFavoritesRecord
import ru.zveron.exception.FavoritesException
import ru.zveron.favorites.lot.AddLotToFavoritesRequest
import ru.zveron.favorites.lot.GetFavoriteLotsRequest
import ru.zveron.favorites.lot.GetFavoriteLotsResponse
import ru.zveron.favorites.lot.GetLotCounterRequest
import ru.zveron.favorites.lot.GetLotCounterResponse
import ru.zveron.favorites.lot.LotFavoritesServiceGrpcKt
import ru.zveron.favorites.lot.LotsExistInFavoritesRequest
import ru.zveron.favorites.lot.LotsExistInFavoritesResponse
import ru.zveron.favorites.lot.RemoveAllByFavoriteLotRequest
import ru.zveron.favorites.lot.RemoveAllLotsByOwnerRequest
import ru.zveron.favorites.lot.RemoveLotFromFavoritesRequest
import ru.zveron.favorites.lot.favoriteLot
import ru.zveron.favorites.lot.getFavoriteLotsResponse
import ru.zveron.favorites.lot.getLotCounterResponse
import ru.zveron.favorites.lot.lotsExistInFavoritesResponse
import ru.zveron.repository.LotsFavoritesRecordRepository

@Service
class LotsFavoritesService(
    private val lotRepository: LotsFavoritesRecordRepository
) : LotFavoritesServiceGrpcKt.LotFavoritesServiceCoroutineImplBase() {

    override suspend fun addToFavorites(request: AddLotToFavoritesRequest): Empty =
        lotRepository.save(
            LotsFavoritesRecord(
                LotsFavoritesRecord.LotsFavoritesKey(
                    ownerUserId = request.favoritesOwnerId,
                    favoriteLotId = request.favoriteLotId
                )
            )
        ).let { Empty.getDefaultInstance() }

    override suspend fun removeFromFavorites(request: RemoveLotFromFavoritesRequest): Empty =
        try {
            lotRepository.deleteById(
                LotsFavoritesRecord.LotsFavoritesKey(
                    ownerUserId = request.favoritesOwnerId,
                    favoriteLotId = request.favoriteLotId
                )
            ).let { Empty.getDefaultInstance() }
        } catch (e: EmptyResultDataAccessException) {
            throw FavoritesException("Нельзя удалить объявление не из списка избранного")
        }

    override suspend fun existInFavorites(request: LotsExistInFavoritesRequest): LotsExistInFavoritesResponse =
        lotsExistInFavoritesResponse {
            isExists.addAll(request.favoriteLotIdList.map { id ->
                lotRepository.existsById_OwnerUserIdAndId_FavoriteLotId(
                    ownerUserId = request.favoritesOwnerId,
                    favoriteLotId = id
                )
            })
        }

    override suspend fun getFavoriteLots(request: GetFavoriteLotsRequest): GetFavoriteLotsResponse =
        getFavoriteLotsResponse {
            favoriteLots.addAll(
                lotRepository.getAllById_OwnerUserId(request.favoritesOwnerId).map {
                    favoriteLot { id = it.id.favoriteLotId }
                })
        }

    override suspend fun getCounter(request: GetLotCounterRequest): GetLotCounterResponse =
        lotRepository.countAllById_FavoriteLotId(request.id)
            .let { statistics -> getLotCounterResponse { addsToFavoritesCounter = statistics } }

    override suspend fun removeAllByOwner(request: RemoveAllLotsByOwnerRequest): Empty =
        lotRepository.deleteAllById_OwnerUserId(request.id).let { Empty.getDefaultInstance() }

    override suspend fun removeAllByFavoriteLot(request: RemoveAllByFavoriteLotRequest): Empty =
        lotRepository.deleteAllById_FavoriteLotId(request.id).let { Empty.getDefaultInstance() }
}