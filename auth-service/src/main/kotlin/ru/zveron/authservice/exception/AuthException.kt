package ru.zveron.authservice.exception

import io.grpc.Status

open class AuthException(message: String?, val code: Status.Code) :
    RuntimeException("Exception with message=$message and code=$code")
