package ru.zveron.authservice.webclient.thirdparty.model

import com.fasterxml.jackson.annotation.JsonAlias

data class UserInfoMailru(
    @JsonAlias("id")
    override val providerUserId: String,

    override val firstName: String?,

    override val lastName: String?,

    override val email: String?,

    @JsonAlias("image")
    override val picture: String?,

    val nickname: String?,
    val name: String?,
    val locale: String?,
    val gender: String?,
    val birthday: String?,
) : UserInfo(
    providerUserId = providerUserId,
    firstName = firstName,
    lastName = lastName,
    picture = picture,
    email = email,
)
