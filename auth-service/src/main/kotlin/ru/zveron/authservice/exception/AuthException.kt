package ru.zveron.authservice.exception

import io.grpc.Metadata
import io.grpc.Status

open class AuthException(message: String?, val code: Status.Code, val metadata: Metadata? = null) :
    RuntimeException("Exception with message=$message and code=$code")
