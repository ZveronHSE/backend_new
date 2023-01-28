package ru.zv.authservice.exceptions

import io.grpc.Status

class InvalidTokenException(
    message: String = "Token verification failed",
    code: Status.Code = Status.Code.UNAUTHENTICATED
) : AuthException(message, code)