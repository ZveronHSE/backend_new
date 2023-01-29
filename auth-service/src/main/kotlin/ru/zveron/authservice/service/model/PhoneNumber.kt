package ru.zveron.authservice.service.model

data class PhoneNumber(
    val countryCode: Int,
    val phone: Long,
) {
    fun toClientPhone() = "$countryCode$phone"
}
