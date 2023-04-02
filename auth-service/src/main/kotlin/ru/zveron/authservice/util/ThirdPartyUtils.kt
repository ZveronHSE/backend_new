package ru.zveron.authservice.util

import org.springframework.web.util.UriComponentsBuilder

object ThirdPartyUtils {
    fun buildGetUserInfoUrl(baseUrl: String, accessToken: String) = UriComponentsBuilder.fromHttpUrl(baseUrl)
        .queryParam("access_token", accessToken)
        .build()
        .toUri()
}