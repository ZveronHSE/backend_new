package ru.zveron.service.presentation.external

import com.google.protobuf.Empty
import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.favorites.profile.AddProfileToFavoritesRequest
import ru.zveron.favorites.profile.GetFavoriteProfilesResponse
import ru.zveron.favorites.profile.ProfileFavoritesServiceExternalGrpcKt
import ru.zveron.favorites.profile.RemoveProfileFromFavoritesRequest
import ru.zveron.favorites.profile.getFavoriteProfilesResponse
import ru.zveron.service.application.ProfileFavoritesService
import ru.zveron.service.presentation.api.SecuredService
import ru.zveron.service.presentation.api.SecuredServiceImpl
import kotlin.coroutines.coroutineContext

@GrpcService
class ProfileFavoritesGrpcServiceExternal(
    private val profileService: ProfileFavoritesService,
    private val securedServiceImpl: SecuredServiceImpl,
) : ProfileFavoritesServiceExternalGrpcKt.ProfileFavoritesServiceExternalCoroutineImplBase(),
    SecuredService by securedServiceImpl {

    override suspend fun addToFavorites(request: AddProfileToFavoritesRequest): Empty =
        profileService.addToFavorites(
            coroutineContext.getAuthorizedProfileId(),
            request.id
        ).let { Empty.getDefaultInstance() }

    override suspend fun getFavoriteProfiles(request: Empty): GetFavoriteProfilesResponse =
        profileService.getFavoriteProfiles(coroutineContext.getAuthorizedProfileId())
            .let { getFavoriteProfilesResponse { favoriteProfiles.addAll(it) } }

    override suspend fun removeFromFavorites(request: RemoveProfileFromFavoritesRequest): Empty =
        profileService.removeFromFavorites(
            coroutineContext.getAuthorizedProfileId(),
            request.id
        ).let { Empty.getDefaultInstance() }
}