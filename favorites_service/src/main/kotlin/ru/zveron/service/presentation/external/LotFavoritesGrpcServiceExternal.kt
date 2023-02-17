package ru.zveron.service.presentation.external

import com.google.protobuf.Empty
import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.favorites.lot.AddLotToFavoritesRequest
import ru.zveron.favorites.lot.GetFavoriteLotsResponse
import ru.zveron.favorites.lot.LotFavoritesServiceExternalGrpcKt
import ru.zveron.favorites.lot.RemoveLotFromFavoritesRequest
import ru.zveron.favorites.lot.getFavoriteLotsResponse
import ru.zveron.service.application.LotFavoritesService
import ru.zveron.service.presentation.api.SecuredService
import ru.zveron.service.presentation.api.SecuredServiceImpl
import kotlin.coroutines.coroutineContext

@GrpcService
class LotFavoritesGrpcServiceExternal(
    private val lotFavoritesService: LotFavoritesService,
    private val securedServiceImpl: SecuredServiceImpl,
) : LotFavoritesServiceExternalGrpcKt.LotFavoritesServiceExternalCoroutineImplBase(),
    SecuredService by securedServiceImpl {

    override suspend fun addToFavorites(request: AddLotToFavoritesRequest): Empty =
        lotFavoritesService.addToFavorites(coroutineContext.getAuthorizedProfileId(), request.id)
            .let { Empty.getDefaultInstance() }

    override suspend fun getFavoriteLots(request: Empty): GetFavoriteLotsResponse = getFavoriteLotsResponse {
        favoriteLots.addAll(lotFavoritesService.getFavorites(coroutineContext.getAuthorizedProfileId()))
    }

    override suspend fun removeFromFavorites(request: RemoveLotFromFavoritesRequest): Empty =
        lotFavoritesService.removeFromFavorites(coroutineContext.getAuthorizedProfileId(), request.id)
            .let { Empty.getDefaultInstance() }
}