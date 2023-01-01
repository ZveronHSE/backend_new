package ru.zveron.service

import com.google.protobuf.Empty
import org.apache.commons.lang3.RandomUtils
import org.springframework.stereotype.Service
import ru.zveron.AddLotToFavoritesRequest
import ru.zveron.GetLotFavoritesCounterRequest
import ru.zveron.GetLotFavoritesCounterResponse
import ru.zveron.ListFavoriteLotsRequest
import ru.zveron.ListFavoriteLotsResponse
import ru.zveron.LotExistsInFavoritesRequest
import ru.zveron.LotExistsInFavoritesResponse
import ru.zveron.LotFavoritesServiceGrpcKt
import ru.zveron.RemoveAllByFavoriteLotRequest
import ru.zveron.RemoveAllLotsByOwnerRequest
import ru.zveron.RemoveLotFromFavoritesRequest
import ru.zveron.entity.LotsFavoritesRecord
import ru.zveron.entity.LotsFavoritesCounter
import ru.zveron.favoriteLot
import ru.zveron.getLotFavoritesCounterResponse
import ru.zveron.listFavoriteLotsResponse
import ru.zveron.lotExistsInFavoritesResponse
import ru.zveron.repository.LotsFavoritesCounterRepository
import ru.zveron.repository.LotsFavoritesRecordRepository
import javax.transaction.Transactional

@Service
class LotsFavoritesService(
    private val lotRepository: LotsFavoritesRecordRepository,
    private val lotCounterRepository: LotsFavoritesCounterRepository
) : LotFavoritesServiceGrpcKt.LotFavoritesServiceCoroutineImplBase() {

    companion object {
        const val SHARDS_NUMBER = 4
    }

    @Transactional
    override suspend fun addLotToFavorites(request: AddLotToFavoritesRequest): Empty {
        val key = LotsFavoritesRecord.LotsFavoritesKey(
            ownerUserId = request.favoritesOwnerId,
            favoriteLotId = request.favoriteLotId
        )

        if (!lotCounterRepository.existsById_LotId(request.favoriteLotId)) {
            add2FavoritesAndCreateShards(key)
            return Empty.getDefaultInstance()
        }

        if (!lotRepository.existsById(key)) {
            add2FavoritesAndIncrementShard(key)
        }

        return Empty.getDefaultInstance()
    }

    @Transactional
    override suspend fun removeLotFromFavorites(request: RemoveLotFromFavoritesRequest): Empty =
        LotsFavoritesRecord.LotsFavoritesKey(
            ownerUserId = request.favoritesOwnerId,
            favoriteLotId = request.favoriteLotId
        ).let {
            if (lotRepository.existsById(it)) {
                removeFromFavoritesAndDecrementShard(it)
            }

            Empty.getDefaultInstance()
        }

    override suspend fun lotExistsInFavorites(request: LotExistsInFavoritesRequest): LotExistsInFavoritesResponse =
        lotRepository.existsById(
            LotsFavoritesRecord.LotsFavoritesKey(
                ownerUserId = request.favoritesOwnerId,
                favoriteLotId = request.favoriteLotId
            )
        ).let { lotExistsInFavoritesResponse { lotExists = it } }

    override suspend fun listFavoriteLots(request: ListFavoriteLotsRequest): ListFavoriteLotsResponse =
        listFavoriteLotsResponse {
            favoriteLots.addAll(
                lotRepository.getAllById_OwnerUserId(request.favoritesOwnerId).map {
                    favoriteLot { lotId = it.id.favoriteLotId }
                })
        }

    override suspend fun getLotFavoritesCounter(request: GetLotFavoritesCounterRequest): GetLotFavoritesCounterResponse =
        lotCounterRepository.getLotFavoritesStatistics(request.lotId)
            .let { statistics -> getLotFavoritesCounterResponse { addsToFavoritesCounter = statistics } }

    @Transactional
    override suspend fun removeAllLotsByOwner(request: RemoveAllLotsByOwnerRequest): Empty {
        lotRepository.getAllById_OwnerUserId(request.profileId).forEach {
            lotCounterRepository.decrementFavoriteCounter(it.id.favoriteLotId, RandomUtils.nextInt(0, SHARDS_NUMBER))
        }
        lotRepository.deleteAllById_OwnerUserId(request.profileId)

        return Empty.getDefaultInstance()
    }

    @Transactional
    override suspend fun removeAllByFavoriteLot(request: RemoveAllByFavoriteLotRequest): Empty {
        lotRepository.deleteAllById_FavoriteLotId(request.lotId)
        lotCounterRepository.zeroAllLotShards(request.lotId)

        return Empty.getDefaultInstance()
    }

    private fun add2FavoritesAndCreateShards(key: LotsFavoritesRecord.LotsFavoritesKey) {
        lotRepository.save(LotsFavoritesRecord(key))
        lotCounterRepository.saveAll(List(SHARDS_NUMBER) { i ->
            LotsFavoritesCounter(
                LotsFavoritesCounter.LotsFavoritesCounterKey(key.favoriteLotId, i),
                (i + 1) / SHARDS_NUMBER.toLong()
            )
        })
    }

    private fun add2FavoritesAndIncrementShard(key: LotsFavoritesRecord.LotsFavoritesKey) {
        lotRepository.save(LotsFavoritesRecord(key))
        lotCounterRepository.incrementFavoriteCounter(key.favoriteLotId, RandomUtils.nextInt(0, SHARDS_NUMBER))
    }

    private fun removeFromFavoritesAndDecrementShard(key: LotsFavoritesRecord.LotsFavoritesKey) {
        lotRepository.delete(LotsFavoritesRecord(key))
        lotCounterRepository.decrementFavoriteCounter(key.favoriteLotId, RandomUtils.nextInt(0, SHARDS_NUMBER))
    }
}