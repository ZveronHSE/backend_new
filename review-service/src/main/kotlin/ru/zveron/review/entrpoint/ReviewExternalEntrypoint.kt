package ru.zveron.review.entrpoint

import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.contract.review.external.CreateLotReviewRequest
import ru.zveron.contract.review.external.CreateLotReviewResponse
import ru.zveron.contract.review.external.CreateProfileReviewRequest
import ru.zveron.contract.review.external.CreateProfileReviewResponse
import ru.zveron.contract.review.external.GetLotReviewByIdRequest
import ru.zveron.contract.review.external.GetLotReviewByIdResponse
import ru.zveron.contract.review.external.GetLotReviewByIdResponseKt
import ru.zveron.contract.review.external.GetProfileReviewByIdRequest
import ru.zveron.contract.review.external.GetProfileReviewByIdResponse
import ru.zveron.contract.review.external.GetProfileReviewByIdResponseKt
import ru.zveron.contract.review.external.ReviewServiceExternalGrpcKt
import ru.zveron.contract.review.external.createLotReviewResponse
import ru.zveron.contract.review.external.createProfileReviewResponse
import ru.zveron.contract.review.external.getLotReviewByIdResponse
import ru.zveron.contract.review.external.getProfileReviewByIdResponse
import ru.zveron.library.grpc.util.GrpcUtils
import ru.zveron.review.persistence.entity.ProfileReview
import ru.zveron.review.persistence.repository.FullReview
import ru.zveron.review.service.CreateReviewRequest
import ru.zveron.review.service.ReviewService
import kotlin.coroutines.coroutineContext

@GrpcService
class ReviewExternalEntrypoint(
    private val service: ReviewService,
) : ReviewServiceExternalGrpcKt.ReviewServiceExternalCoroutineImplBase() {

    override suspend fun createLotReview(request: CreateLotReviewRequest): CreateLotReviewResponse {
        val userId = GrpcUtils.getMetadata(coroutineContext, requiredAuthorized = true).profileId!!

        val serviceResponse = service.createLotReview(request.toServiceRequest(userId))

        return createLotReviewResponse { this.id = serviceResponse }
    }

    override suspend fun getLotReviewById(request: GetLotReviewByIdRequest): GetLotReviewByIdResponse {
        val serviceResponse = service.getLotReview(request.reviewId)

        return GetLotReviewByIdResponseKt.of(serviceResponse)
    }

    override suspend fun createProfileReview(request: CreateProfileReviewRequest): CreateProfileReviewResponse {
        val userId = GrpcUtils.getMetadata(coroutineContext, requiredAuthorized = true).profileId!!

        val serviceResponse = service.createProfileReview(request.toServiceRequest(userId))

        return createProfileReviewResponse { this.id = serviceResponse.id ?: error("should not happen") }
    }

    override suspend fun getProfileReviewById(request: GetProfileReviewByIdRequest): GetProfileReviewByIdResponse {
        val serviceResponse = service.getProfileReviews(request.profileId)

        //todo: change
        return GetProfileReviewByIdResponseKt.of(serviceResponse.first())
    }
}

fun CreateLotReviewRequest.toServiceRequest(reviewerId: Long) = CreateReviewRequest(
    lotId = lotId,
    reviewerProfileId = reviewerId,
    text = text,
    score = score,
    imageUrls = imageUrlsList,
)

fun CreateProfileReviewRequest.toServiceRequest(reviewerId: Long) = ru.zveron.review.service.CreateProfileReviewRequest(
    profileId = profileId,
    reviewerProfileId = reviewerId,
    text = text,
    score = score,
)

fun GetProfileReviewByIdResponseKt.of(r: ProfileReview) = getProfileReviewByIdResponse {
    this.profileReview = profileReview {
        this.id = r.id ?: error("Should not happen")
        this.reviewerId = r.reviewerProfileId
        this.text = r.text
        this.profileId = r.profileId
    }
}


fun GetLotReviewByIdResponseKt.of(r: FullReview) = getLotReviewByIdResponse {
    this.review = review {
        this.id = r.review.id ?: error("Should not happen")
        this.reviewerId = r.review.reviewerProfileId
        this.test = r.review.text
        this.score = r.review.score
    }

    this.imageUrls.addAll(r.images.map { it.imageUrl })
}
