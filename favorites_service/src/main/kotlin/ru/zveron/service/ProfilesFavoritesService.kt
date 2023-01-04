package ru.zveron.service

import com.google.protobuf.Empty
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Service
import ru.zveron.entity.ProfilesFavoritesRecord
import ru.zveron.exception.FavoritesException
import ru.zveron.favorites.profile.AddProfileToFavoritesRequest
import ru.zveron.favorites.profile.GetFavoriteProfilesRequest
import ru.zveron.favorites.profile.GetFavoriteProfilesResponse
import ru.zveron.favorites.profile.ProfileExistsInFavoritesRequest
import ru.zveron.favorites.profile.ProfileExistsInFavoritesResponse
import ru.zveron.favorites.profile.ProfileFavoritesServiceGrpcKt
import ru.zveron.favorites.profile.RemoveAllByFavoriteProfileRequest
import ru.zveron.favorites.profile.RemoveAllProfilesByOwnerRequest
import ru.zveron.favorites.profile.RemoveProfileFromFavoritesRequest
import ru.zveron.favorites.profile.favoriteProfile
import ru.zveron.favorites.profile.getFavoriteProfilesResponse
import ru.zveron.favorites.profile.profileExistsInFavoritesResponse
import ru.zveron.repository.ProfilesFavoritesRecordRepository

@Service
class ProfilesFavoritesService(private val profileRepository: ProfilesFavoritesRecordRepository) :
    ProfileFavoritesServiceGrpcKt.ProfileFavoritesServiceCoroutineImplBase() {

    override suspend fun addToFavorites(request: AddProfileToFavoritesRequest): Empty =
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

    override suspend fun removeFromFavorites(request: RemoveProfileFromFavoritesRequest): Empty =
        request.takeUnless { request.favoritesOwnerId == request.favoriteProfileId }?.let {
            try {
                profileRepository.deleteById(
                    ProfilesFavoritesRecord.ProfilesFavoritesKey(
                        ownerUserId = request.favoritesOwnerId,
                        favoriteUserId = request.favoriteProfileId
                    )
                )
                Empty.getDefaultInstance()
            } catch (e: EmptyResultDataAccessException) {
                throw FavoritesException("Нельзя удалить профиль не из списка избранного")
            }
        } ?: throw FavoritesException("Нельзя удалить себя из своего списка избранного")

    override suspend fun existsInFavorites(request: ProfileExistsInFavoritesRequest): ProfileExistsInFavoritesResponse =
        request.takeUnless { request.favoritesOwnerId == request.favoriteProfileId }?.let {
            val exists = profileRepository.existsById(
                ProfilesFavoritesRecord.ProfilesFavoritesKey(
                    ownerUserId = request.favoritesOwnerId,
                    favoriteUserId = request.favoriteProfileId
                )
            )
            profileExistsInFavoritesResponse { isExists = exists }
        } ?: throw FavoritesException("Профиль не может быть в собственном списке избранного")

    override suspend fun getFavoriteProfiles(request: GetFavoriteProfilesRequest): GetFavoriteProfilesResponse =
        getFavoriteProfilesResponse {
            favoriteProfiles.addAll(
                profileRepository.getAllById_OwnerUserId(request.id).map {
                    favoriteProfile { id = it.id.favoriteUserId }
                })
        }

    override suspend fun removeAllByOwner(request: RemoveAllProfilesByOwnerRequest): Empty =
        profileRepository.deleteAllById_OwnerUserId(request.id).let { Empty.getDefaultInstance() }

    override suspend fun removeAllByFavoriteProfile(request: RemoveAllByFavoriteProfileRequest): Empty =
        profileRepository.deleteAllById_FavoriteUserId(request.id).let { Empty.getDefaultInstance() }
}