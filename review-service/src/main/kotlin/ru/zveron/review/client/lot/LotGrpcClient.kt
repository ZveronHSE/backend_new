package ru.zveron.review.client.lot

import io.grpc.Status
import io.grpc.StatusException
import ru.zveron.contract.core.LotForReview
import ru.zveron.contract.lot.LotInternalServiceGrpcKt
import ru.zveron.contract.lot.getLotForReviewRequest

class LotGrpcClient(
    private val client: LotInternalServiceGrpcKt.LotInternalServiceCoroutineStub,
) {

    suspend fun getLot(lotId: Long): GetLotResponseApi {
        val request = getLotForReviewRequest { this.lotId = lotId }

        return try {
            val response = client.getLotForReview(request)
            GetLotResponseApi.Success(lot = response.lot.toLot())
        } catch (ex: StatusException) {
            when (ex.status.code) {
                Status.Code.NOT_FOUND -> GetLotResponseApi.NotFound
                else -> GetLotResponseApi.Error(ex.status, ex.message)
            }
        }

    }
}

sealed class GetLotResponseApi {
    data class Success(val lot: Lot) : GetLotResponseApi()
    object NotFound : GetLotResponseApi()
    data class Error(val status: Status, val message: String?) : GetLotResponseApi()
}

data class Lot(
    val lotId: Long,
    val sellerId: Long,
    val lotStatus: LotStatus,
)

fun LotForReview.toLot() = Lot(
    lotId = this.id,
    sellerId = this.sellerId,
    lotStatus = this.status.toStatus(),
)

fun ru.zveron.contract.core.Status.toStatus() = when (this) {
    ru.zveron.contract.core.Status.ACTIVE -> LotStatus.ACTIVE
    ru.zveron.contract.core.Status.CLOSED -> LotStatus.CLOSED
    ru.zveron.contract.core.Status.CANCELED -> LotStatus.CANCELED
    ru.zveron.contract.core.Status.UNRECOGNIZED -> error("Unknown status for lot $this")
}

enum class LotStatus {
    ACTIVE,
    CLOSED,
    CANCELED,
    ;
}