package ru.zveron.authservice.service.dto

data class PhoneNumber(
    val countryCode: Int,
    val phone: Long,
) {
    fun toClientPhone() = "$countryCode$phone"
}

fun PhoneNumber.toContext() =
    ru.zveron.authservice.persistence.model.PhoneNumber(countryCode = countryCode.toString(), phone = phone.toString())
