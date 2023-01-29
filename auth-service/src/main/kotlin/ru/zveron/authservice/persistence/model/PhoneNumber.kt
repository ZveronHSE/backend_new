package ru.zveron.authservice.persistence.model

data class PhoneNumber(
    val countryCode: String,
    val phone: String,
)

fun PhoneNumber.toClient() = "${this.countryCode}${this.phone}"
