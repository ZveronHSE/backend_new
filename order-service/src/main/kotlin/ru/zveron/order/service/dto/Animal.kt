package ru.zveron.order.service.dto

data class Animal(
    val id: Long,
    val name: String,
    val species: String,
    val breed: String,
    val imageUrl: String,
){
    companion object
}