package ru.zveron.authservice.exception

import io.grpc.Status

class TokenExpiredException(
    message: String = "Token expired",
    code: Status.Code = Status.Code.UNAUTHENTICATED
) : AuthException(message, code)
