package ru.zveron.review.component

import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import org.springframework.stereotype.Component
import ru.zveron.review.client.lot.GetLotResponseApi
import ru.zveron.review.client.lot.LotGrpcClient
import ru.zveron.review.client.profile.ProfileGrpcClient
import ru.zveron.review.client.profile.dto.GetProfileApiResponse

@Component
class ClientDecorator(
    private val profileGrpcClient: ProfileGrpcClient,
    private val lotGrpcClient: LotGrpcClient,
) {


    suspend fun validateReviewLegitimacy(profileId: Long, lotId: Long) = supervisorScope {
        val profileDeferred = async { getProfile(profileId) }
        val lotDeferred = async { getLot(lotId) }
        profileDeferred.await()
        val lot = lotDeferred.await()

        if (lot.sellerId == profileId) {
            throw Exception("Seller can't review his own lot")
        }

        if (lot.lotStatus != ru.zveron.review.client.lot.LotStatus.CLOSED) {
            throw Exception("Lot must be sold to be reviewed")
        }
    }

    private suspend fun getProfile(profileId: Long) {
        val clientResponse = profileGrpcClient.getProfile(profileId)

        when (clientResponse) {
            is GetProfileApiResponse.Error -> throw Exception("Unknown exception")
            GetProfileApiResponse.NotFound -> throw Exception("Profile not found for id=$profileId")
            is GetProfileApiResponse.Success -> {} //do nothing
        }
    }

    private suspend fun getLot(lotId: Long): ru.zveron.review.client.lot.Lot {
        val clientResponse = lotGrpcClient.getLot(lotId)

        return when (clientResponse) {
            is GetLotResponseApi.Error -> throw Exception("Client exeption: ${clientResponse.message}")
            GetLotResponseApi.NotFound -> throw Exception("Lot not found by id=$lotId")
            is GetLotResponseApi.Success -> clientResponse.lot
        }
    }
}
