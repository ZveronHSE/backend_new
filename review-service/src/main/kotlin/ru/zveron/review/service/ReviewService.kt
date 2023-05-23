package ru.zveron.review.service

import org.springframework.stereotype.Service
import ru.zveron.review.component.ClientDecorator
import ru.zveron.review.persistence.entity.LotReview
import ru.zveron.review.persistence.entity.ProfileReview
import ru.zveron.review.persistence.repository.FullReview
import ru.zveron.review.persistence.repository.ReviewStorage

@Service
class ReviewService(
    private val reviewStorage: ReviewStorage,
    private val clientDecorator: ClientDecorator,
) {

    suspend fun createLotReview(request: CreateReviewRequest): Long {
        clientDecorator.validateReviewLegitimacy(request.reviewerProfileId, request.lotId)

        return reviewStorage.createLotReviewWithImages(request.toEntity(), request.imageUrls).review.id
            ?: error("should not happen")
    }

    suspend fun createProfileReview(request: CreateProfileReviewRequest): ProfileReview {
        return reviewStorage.createProfileReview(request.toEntity())
    }

    suspend fun getLotReview(reviewId: Long): FullReview {
        return reviewStorage.findReviewById(reviewId)
    }

    suspend fun getProfileReviews(profileId: Long): List<ProfileReview> {
        return reviewStorage.findProfileReviewsByProfileId(profileId)
    }

    suspend fun calculateProfileRating(profileId: Long): Double {
        return reviewStorage.getProfileRating(profileId)
    }

    suspend fun calculateLotRating(lotId: Long): Double {
        return reviewStorage.getLotRating(lotId)
    }
}

data class CreateReviewRequest(
    val lotId: Long,
    val reviewerProfileId: Long,
    val text: String,
    val score: Int,
    val imageUrls: List<String>,
)

data class CreateProfileReviewRequest(
    val profileId: Long,
    val reviewerProfileId: Long,
    val text: String,
    val score: Int,
)

fun CreateProfileReviewRequest.toEntity() = ProfileReview(
    profileId = profileId,
    reviewerProfileId = reviewerProfileId,
    text = text,
    score = score,
)


fun CreateReviewRequest.toEntity() = LotReview(
    lotId = lotId,
    reviewerProfileId = reviewerProfileId,
    text = text,
    score = score,
)