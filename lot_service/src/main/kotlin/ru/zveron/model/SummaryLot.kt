package ru.zveron.model

import java.time.Instant

data class SummaryLot(
    val id: Long,
    val title: String,
    val price: Int,
    val createdAt: Instant,
    val photoId: Long
)