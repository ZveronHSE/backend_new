package ru.zv.authservice.exceptions

import io.grpc.Status

class AuthException(message: String?, val code: Status.Code) :
    RuntimeException("Exception with message=$message and code=$code")
