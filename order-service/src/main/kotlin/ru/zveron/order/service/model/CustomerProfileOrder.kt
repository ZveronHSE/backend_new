package ru.zveron.order.service.model

import ru.zveron.order.persistence.model.constant.Status
import java.time.Instant
import java.time.LocalDate

data class CustomerProfileOrder(
    val id: Long,
    val animal: Animal,
    val price: Long,
    val title: String,
    val subway: SubwayStation?,
    val createdAt: Instant,
    val serviceDateFrom: LocalDate,
    val serviceDateTo: LocalDate?,
    val status: Status,
)
