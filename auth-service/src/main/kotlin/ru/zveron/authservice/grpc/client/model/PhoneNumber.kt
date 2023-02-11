package ru.zveron.authservice.grpc.client.model

data class PhoneNumber(
    val countryCode: String,
    val phone: String,
) {
    companion object {
        fun of(p: ru.zveron.authservice.persistence.model.PhoneNumber) = PhoneNumber(
            countryCode = p.countryCode,
            phone = p.phone
        )
    }
}