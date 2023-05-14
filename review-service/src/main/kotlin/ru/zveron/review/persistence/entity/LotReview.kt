package ru.zveron.review.persistence.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("lot_review")
data class LotReview(
    @Id
    val id: Long? = null,
    val lotId: Long,
    val reviewerProfileId: Long,

    val text: String,

    val score: Int,

    @CreatedDate
    val createdAt: Instant = Instant.now(),
)
