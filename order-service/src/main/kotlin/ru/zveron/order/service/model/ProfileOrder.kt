package ru.zveron.order.service.model

data class ProfileOrder(
    val orderLotId: Long,
    val title: String,
    val price: String,
    val imageUrl: String,
    val viewCount: Long,
)