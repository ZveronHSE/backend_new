package ru.zv.authservice.exceptions

import io.grpc.Status

class SessionExpiredException(
    message: String = "Session has expired",
    code: Status.Code = Status.Code.UNAUTHENTICATED,
) : AuthException(message, code)