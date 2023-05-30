package ru.zveron.review.persistence.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.zveron.review.persistence.entity.ReviewImage

@JvmDefaultWithCompatibility
interface ReviewImageRepository: CoroutineCrudRepository<ReviewImage, Long> {
    suspend fun saveAll(images: List<ReviewImage>): List<ReviewImage>

    suspend fun findAllByReviewId(reviewId: Long): List<ReviewImage>
}