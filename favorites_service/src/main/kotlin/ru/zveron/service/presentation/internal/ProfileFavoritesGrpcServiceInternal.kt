package ru.zveron.service.presentation.internal

import com.google.protobuf.Empty
import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.favorites.profile.ProfileExistsInFavoritesRequest
import ru.zveron.favorites.profile.ProfileExistsInFavoritesResponse
import ru.zveron.favorites.profile.ProfileFavoritesServiceInternalGrpcKt
import ru.zveron.favorites.profile.RemoveAllByFavoriteProfileRequest
import ru.zveron.favorites.profile.RemoveAllProfilesByOwnerRequest
import ru.zveron.favorites.profile.profileExistsInFavoritesResponse
import ru.zveron.service.application.ProfileFavoritesService

@GrpcService
class ProfileFavoritesGrpcServiceInternal(
    private val profileService: ProfileFavoritesService
) : ProfileFavoritesServiceInternalGrpcKt.ProfileFavoritesServiceInternalCoroutineImplBase() {

    override suspend fun existsInFavorites(request: ProfileExistsInFavoritesRequest): ProfileExistsInFavoritesResponse =
        profileService.existsInFavorites(
            favoritesOwnerId = request.favoritesOwnerId,
            targetUserId = request.favoriteProfileId
        ).let { exists -> profileExistsInFavoritesResponse { isExists = exists } }

    override suspend fun removeAllByFavoriteProfile(request: RemoveAllByFavoriteProfileRequest): Empty =
        profileService.removeAllByFavoriteProfile(request.id).let { Empty.getDefaultInstance() }

    override suspend fun removeAllByOwner(request: RemoveAllProfilesByOwnerRequest): Empty =
        profileService.removeAllByOwner(request.id).let { Empty.getDefaultInstance() }
}