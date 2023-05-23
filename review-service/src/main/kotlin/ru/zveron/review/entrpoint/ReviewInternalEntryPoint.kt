package ru.zveron.review.entrpoint

import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.contract.review.internal.GetRatingByLotIdRequest
import ru.zveron.contract.review.internal.GetRatingByLotIdResponse
import ru.zveron.contract.review.internal.GetRatingByProfileIdRequest
import ru.zveron.contract.review.internal.GetRatingByProfileIdResponse
import ru.zveron.contract.review.internal.RatingServiceExternalGrpcKt
import ru.zveron.contract.review.internal.getRatingByLotIdResponse
import ru.zveron.contract.review.internal.getRatingByProfileIdResponse
import ru.zveron.review.service.ReviewService

@GrpcService
class ReviewInternalEntryPoint(
    private val service: ReviewService,
) : RatingServiceExternalGrpcKt.RatingServiceExternalCoroutineImplBase() {

    override suspend fun getRatingByLotId(request: GetRatingByLotIdRequest): GetRatingByLotIdResponse {
        val serviceResponse = service.calculateLotRating(request.lotId)

        return getRatingByLotIdResponse { this.rating = serviceResponse.toFloat() }
    }

    override suspend fun getRatingByProfileId(request: GetRatingByProfileIdRequest): GetRatingByProfileIdResponse {
        val serviceResponse = service.calculateProfileRating(request.profileId)

        return getRatingByProfileIdResponse {
            this.rating = serviceResponse.toFloat()
        }
    }
}