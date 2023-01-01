package ru.zveron.service

import com.google.protobuf.Empty
import org.springframework.stereotype.Service
import ru.zveron.AddProfileToFavoritesRequest
import ru.zveron.ListFavoriteProfilesRequest
import ru.zveron.ListFavoriteProfilesResponse
import ru.zveron.ProfileExistsInFavoritesRequest
import ru.zveron.ProfileExistsInFavoritesResponse
import ru.zveron.ProfileFavoritesServiceGrpcKt
import ru.zveron.RemoveAllByFavoriteProfileRequest
import ru.zveron.RemoveAllProfilesByOwnerRequest
import ru.zveron.RemoveProfileFromFavoritesRequest
import ru.zveron.entity.ProfilesFavoritesRecord
import ru.zveron.exception.FavoritesException
import ru.zveron.favoriteProfile
import ru.zveron.listFavoriteProfilesResponse
import ru.zveron.profileExistsInFavoritesResponse
import ru.zveron.repository.ProfilesFavoritesRecordRepository

@Service
class ProfilesFavoritesService(private val profileRepository: ProfilesFavoritesRecordRepository) :
    ProfileFavoritesServiceGrpcKt.ProfileFavoritesServiceCoroutineImplBase() {

    override suspend fun addProfileToFavorites(request: AddProfileToFavoritesRequest): Empty =
        request.takeUnless { request.favoritesOwnerId == request.favoriteProfileId }?.let {
            profileRepository.save(
                ProfilesFavoritesRecord(
                    ProfilesFavoritesRecord.ProfilesFavoritesKey(
                        ownerUserId = request.favoritesOwnerId,
                        favoriteUserId = request.favoriteProfileId
                    )
                )
            )
            Empty.getDefaultInstance()
        } ?: throw FavoritesException("Нельзя добавить себя в свой список избранного")

    override suspend fun removeProfileFromFavorites(request: RemoveProfileFromFavoritesRequest): Empty =
        request.takeUnless { request.favoritesOwnerId == request.favoriteProfileId }?.let {
            profileRepository.delete(
                ProfilesFavoritesRecord(
                    ProfilesFavoritesRecord.ProfilesFavoritesKey(
                        ownerUserId = request.favoritesOwnerId,
                        favoriteUserId = request.favoriteProfileId
                    )
                )
            )
            Empty.getDefaultInstance()
        } ?: throw FavoritesException("Нельзя удалиться себя из своего списка избранного")

    override suspend fun profileExistsInFavorites(request: ProfileExistsInFavoritesRequest): ProfileExistsInFavoritesResponse =
        request.takeUnless { request.favoritesOwnerId == request.favoriteProfileId }?.let {
            val exists = profileRepository.existsById(
                ProfilesFavoritesRecord.ProfilesFavoritesKey(
                    ownerUserId = request.favoritesOwnerId,
                    favoriteUserId = request.favoriteProfileId
                )
            )
            profileExistsInFavoritesResponse { profileExists = exists }
        } ?: throw FavoritesException("Профиль не может быть в собственном списке избранного")

    override suspend fun listFavoriteProfiles(request: ListFavoriteProfilesRequest): ListFavoriteProfilesResponse =
        listFavoriteProfilesResponse {
            favoriteProfiles.addAll(
                profileRepository.getAllById_OwnerUserId(request.favoritesOwnerId).map {
                    favoriteProfile { profileId = it.id.favoriteUserId }
                })
        }

    override suspend fun removeAllProfilesByOwner(request: RemoveAllProfilesByOwnerRequest): Empty =
        profileRepository.deleteAllById_OwnerUserId(request.profileId).let { Empty.getDefaultInstance() }

    override suspend fun removeAllByFavoriteProfile(request: RemoveAllByFavoriteProfileRequest): Empty =
        profileRepository.deleteAllById_FavoriteUserId(request.profileId).let { Empty.getDefaultInstance() }
}