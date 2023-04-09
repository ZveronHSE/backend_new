package ru.zveron.authservice.webclient.thirdparty.model

import com.fasterxml.jackson.annotation.JsonAlias

data class UserInfoGoogle(
    @JsonAlias("sub")
    override val providerUserId: String,

    @JsonAlias("name")
    override val firstName: String?,

    @JsonAlias("family_name")
    override val lastName: String?,

    override val picture: String?,

    override val email: String?,

    val email_verified: Boolean,
    val given_name: String?,
    val locale: String?,
) : UserInfo(
    providerUserId = providerUserId,
    firstName = firstName,
    lastName = lastName,
    picture = picture,
    email = email,
)
