package ru.zveron.grpc

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.client.favorite.LotFavoriteClient
import ru.zveron.contract.lot.LotInternalServiceGrpcKt
import ru.zveron.contract.lot.LotsIdRequest
import ru.zveron.contract.lot.LotsIdResponse
import ru.zveron.contract.lot.ProfileLotsRequest
import ru.zveron.contract.lot.ProfileLotsResponse
import ru.zveron.contract.lot.lotsIdResponse
import ru.zveron.mapper.LotMapper
import ru.zveron.service.LotService
import ru.zveron.util.ValidateUtils.validatePositive

@GrpcService
class LotInternalController(
    private val lotService: LotService,
    private val lotFavoriteClient: LotFavoriteClient
) : LotInternalServiceGrpcKt.LotInternalServiceCoroutineImplBase() {
    override suspend fun getLotsById(request: LotsIdRequest): LotsIdResponse {
        if (request.lotIdsCount == 0) {
            return lotsIdResponse { }
        }

        return coroutineScope {
            val favorites = request.userId.takeIf { it > 0 }
                ?.run {
                    async {
                        lotFavoriteClient.checkLotsAreFavorites(request.lotIdsList, this@run)
                    }
                }


            val lots = lotService.getLotsByIds(request.lotIdsList)


            return@coroutineScope LotMapper.buildLotsIdResponse(lots, favorites?.await())
        }
    }

    override suspend fun getLotsBySellerId(request: ProfileLotsRequest): ProfileLotsResponse {
        request.sellerId.validatePositive("sellerId")

        val lots = lotService.getLotsBySellerId(request.sellerId)
        val favorites = request.userId.takeIf { it > 0 }
            ?.run { lotFavoriteClient.checkLotsAreFavorites(lots.map { it.id }, this) }


        return LotMapper.buildProfileLotsResponse(lots, favorites)
    }
}