package ru.zveron.review.persistence.repository

import org.springframework.stereotype.Component
import ru.zveron.review.persistence.entity.LotReview
import ru.zveron.review.persistence.entity.ProfileReview
import ru.zveron.review.persistence.entity.ReviewImage

@Component
class ReviewStorage(
    private val lotReviewRepository: LotReviewRepository,
    private val reviewImageRepository: ReviewImageRepository,
    private val profileReviewRepository: ProfileReviewRepository,
) {

    suspend fun createLotReviewWithImages(review: LotReview, images: List<String>): FullReview {
        val savedReview = lotReviewRepository.save(review)
        val savedImages =
            reviewImageRepository.saveAll(images.map { ReviewImage(reviewId = savedReview.id!!, imageUrl = it) })

        return FullReview(savedReview, savedImages)
    }

    suspend fun createProfileReview(profileReview: ProfileReview): ProfileReview {
        return profileReviewRepository.save(profileReview)
    }

    suspend fun findReviewByProfileId(profileId: Long): List<FullReview> {
        return lotReviewRepository.findAllByReviewerProfileId(profileId).map { review ->
            val images = reviewImageRepository.findAllByReviewId(review.id!!)
            FullReview(review, images)
        }
    }

    suspend fun findReviewById(reviewId: Long): FullReview {
        val review = lotReviewRepository.findById(reviewId) ?: throw Exception("Review with id $reviewId not found")
        val images = reviewImageRepository.findAllByReviewId(review.id!!)

        return FullReview(review, images)
    }

    suspend fun findProfileReviewsByProfileId(profileId: Long): List<ProfileReview> {
        return profileReviewRepository.findAllByReviewerProfileId(profileId)
    }

    suspend fun getProfileRating(profileId: Long): Double {
        return profileReviewRepository.calculateRating(profileId)
    }

    suspend fun getLotRating(lotId: Long): Double {
        return lotReviewRepository.calculateLotRating(lotId)
    }
}

data class FullReview(
    val review: LotReview,
    val images: List<ReviewImage>,
)