package ru.zveron.service.presentation.internal

import com.google.protobuf.Empty
import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.favorites.lot.GetLotCounterRequest
import ru.zveron.favorites.lot.GetLotCounterResponse
import ru.zveron.favorites.lot.LotFavoritesServiceInternalGrpcKt
import ru.zveron.favorites.lot.LotsExistInFavoritesRequest
import ru.zveron.favorites.lot.LotsExistInFavoritesResponse
import ru.zveron.favorites.lot.RemoveAllByFavoriteLotRequest
import ru.zveron.favorites.lot.RemoveAllLotsByOwnerRequest
import ru.zveron.favorites.lot.getLotCounterResponse
import ru.zveron.favorites.lot.lotsExistInFavoritesResponse
import ru.zveron.service.application.LotFavoritesService

@GrpcService
class LotFavoritesGrpcServiceInternal(
    private val lotFavoritesService: LotFavoritesService,
) : LotFavoritesServiceInternalGrpcKt.LotFavoritesServiceInternalCoroutineImplBase() {

    override suspend fun existInFavorites(request: LotsExistInFavoritesRequest): LotsExistInFavoritesResponse =
        lotsExistInFavoritesResponse {
            isExists.addAll(lotFavoritesService.existsInFavorites(request.favoritesOwnerId, request.favoriteLotIdList))
        }

    override suspend fun getCounter(request: GetLotCounterRequest): GetLotCounterResponse = getLotCounterResponse {
        addsToFavoritesCounter = lotFavoritesService.getCounter(request.id)
    }

    override suspend fun removeAllByFavoriteLot(request: RemoveAllByFavoriteLotRequest): Empty =
        lotFavoritesService.removeAllByLot(request.id).let { Empty.getDefaultInstance() }

    override suspend fun removeAllByOwner(request: RemoveAllLotsByOwnerRequest): Empty =
        lotFavoritesService.removeAllByOwner(request.id).let { Empty.getDefaultInstance() }
}