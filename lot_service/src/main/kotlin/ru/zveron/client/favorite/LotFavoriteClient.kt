package ru.zveron.client.favorite

import io.grpc.Status
import io.grpc.StatusException
import org.springframework.stereotype.Service
import ru.zveron.exception.LotException
import ru.zveron.favorites.lot.LotFavoritesServiceInternalGrpcKt
import ru.zveron.favorites.lot.LotsExistInFavoritesRequest
import ru.zveron.favorites.lot.LotsExistInFavoritesResponse
import ru.zveron.favorites.lot.lotsExistInFavoritesRequest

@Service
class LotFavoriteClient(
    val lotFavoriteStub: LotFavoritesServiceInternalGrpcKt.LotFavoritesServiceInternalCoroutineStub
) {
    /**
     * В ответе берем только самый первый элемент, потому что передаем только один идентификатор для объявления
     * Т.е. кейс - карточка товара, мне нужно знать в избранном он или нет, поэтому иду в батчевую ручку
     */
    suspend fun checkLotIsFavorite(lotId: Long, userId: Long): Boolean {
        val request = lotsExistInFavoritesRequest {
            favoriteLotId.add(lotId)
            favoritesOwnerId = userId
        }

        val response = callExistInFavorites(request)

        return response.isExistsList[0]
    }

    suspend fun checkLotsAreFavorites(lotIds: List<Long>, userId: Long): List<Boolean> {
        val request = lotsExistInFavoritesRequest {
            favoriteLotId.addAll(lotIds)
            favoritesOwnerId = userId
        }

        val response = callExistInFavorites(request)

        return response.isExistsList
    }

    private suspend fun callExistInFavorites(request: LotsExistInFavoritesRequest): LotsExistInFavoritesResponse {
        return try {
            val response = lotFavoriteStub.existInFavorites(request)

            if (response.isExistsCount != request.favoriteLotIdCount) {
                throw LotException(
                    Status.INTERNAL,
                    "Quantity of lots not same with quantity of favorites: " +
                            "${response.isExistsCount} != ${request.favoriteLotIdCount}"
                )
            } else {
                response
            }
        } catch (ex: StatusException) {
            throw LotException(
                Status.INTERNAL,
                "Can't get answer from favorites for userId=${request.favoritesOwnerId}, " +
                        "lotId=${request.favoriteLotIdList[0]}. Status: ${ex.status.description}"
            )
        }
    }
}
