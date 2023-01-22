package ru.zv.authservice.persistence.model

data class PhoneNumber(
    val countryCode: Int,
    val phone: Long,
)
