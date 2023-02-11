package ru.zveron.model

data class Address(
    val id: Long,
    // Отформатированный адрес в виде 15 января 2022 
    val address: String,
    val latitude: Double,
    val longitude: Double,
)