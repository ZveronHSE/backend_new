package ru.zv.authservice.service.dto

data class PhoneNumber(
    val countryCode: Int,
    val phone: Long,
) {
    fun toClientPhone() = "$countryCode$phone"
}

fun PhoneNumber.toContext() = ru.zv.authservice.persistence.model.PhoneNumber(countryCode = countryCode, phone = phone)
