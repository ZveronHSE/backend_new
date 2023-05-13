package ru.zveron.order.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("statistics")
data class Statistics(
    @Id
    val id: Long? = null,
    val orderLotId: Long,
    val viewCount: Long = 0,
)
