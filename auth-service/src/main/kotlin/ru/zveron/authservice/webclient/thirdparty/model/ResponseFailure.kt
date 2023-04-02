package ru.zveron.authservice.webclient.thirdparty.model

import org.springframework.http.HttpStatus

interface ResponseFailure {
    fun getHttpStatusCode(): HttpStatus?
    fun getMessage(): String?
}