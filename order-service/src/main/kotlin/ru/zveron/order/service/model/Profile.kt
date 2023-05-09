package ru.zveron.order.service.model

data class Profile(
    val id: Long,
    val name: String,
    val imageUrl: String,
    val rating: Double,
){
    companion object
}
