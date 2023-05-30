package ru.zveron.service.model

import ru.zveron.persistence.model.constant.Status
import java.time.Instant
import java.time.LocalDate

data class ProfileOrder(
    val orderLotId: Long,
    val title: String,
    val price: String,
    val imageUrl: String,
    val viewCount: Long,
    val status: Status,
    val animal: Animal,
    val createdAt: Instant,
    val serviceDateFrom: LocalDate,
    val serviceDateTo: LocalDate,
)