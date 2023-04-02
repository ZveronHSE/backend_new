package ru.zveron.authservice.webclient.thirdparty.model

open class UserInfo(
    open val providerUserId: String,
    open val firstName: String?,
    open val lastName: String?,
    open val picture: String?,
    open val email: String?,
)