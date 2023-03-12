package ru.zveron.authservice.webclient.thirdparty.model

data class UserInfoGoogle(
    val email: String,
    val email_verified: Boolean,
    val family_name: String,
    val given_name: String,
    val locale: String,
    val name: String,
    val picture: String,
    val sub: String
)