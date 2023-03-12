package ru.zveron.authservice.webclient.thirdparty.model

import org.springframework.http.HttpStatus

sealed class GetThirdPartyUserInfoResponse<T>
data class GetThirdPartyUserInfoFailure<T>(
    val code: HttpStatus?,
    val errorMessage: String?,
) : GetThirdPartyUserInfoResponse<T>()

data class GetThirdPartyUserInfoSuccess<T>(
    val response: T,
) : GetThirdPartyUserInfoResponse<T>()