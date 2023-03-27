package ru.zveron.authservice.component.thirdparty.model

data class ThirdPartyUserInfo(
    val firstName: String,
    val lastName: String,
    val userId: String,
    val email: String? = null,
) {
    companion object Factory
}
