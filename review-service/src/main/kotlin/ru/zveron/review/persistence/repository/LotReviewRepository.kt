package ru.zveron.review.persistence.repository

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.zveron.review.persistence.entity.LotReview

@JvmDefaultWithCompatibility
interface LotReviewRepository: CoroutineCrudRepository<LotReview, Long> {

    suspend fun findAllByReviewerProfileId(reviewerProfileId: Long): List<LotReview>

    @Query(
        """select avg(score) from lot_review where lot_id = :lotId"""
    )
    suspend fun calculateLotRating(lotId: Long): Double
}
