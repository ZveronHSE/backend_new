package ru.zveron.order.service.model

data class SubwayStation(
    val id: Int,
    val town: String,
    val name: String,
    val colorHex: String,
) {
    companion object
}
