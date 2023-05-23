package ru.zveron.review.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table
data class ReviewImage(
    @Id
    val id: Long? = null,

    val reviewId: Long,
    val imageUrl: String,
)