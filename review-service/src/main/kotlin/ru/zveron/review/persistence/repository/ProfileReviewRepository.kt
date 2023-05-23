package ru.zveron.review.persistence.repository

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.zveron.review.persistence.entity.ProfileReview

@JvmDefaultWithCompatibility
interface ProfileReviewRepository : CoroutineCrudRepository<ProfileReview, Long> {

    suspend fun findAllByReviewerProfileId(reviewerProfileId: Long): List<ProfileReview>

    @Query(
        """select avg(score) from profile_review where profile_id = :profileId"""
    )
    suspend fun calculateRating(profileId: Long): Double
}