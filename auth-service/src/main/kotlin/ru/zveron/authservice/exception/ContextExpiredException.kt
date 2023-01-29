package ru.zveron.authservice.exception

import io.grpc.Status

class ContextExpiredException(
    message: String = "State context has expired or does not exist",
    code: Status.Code = Status.Code.NOT_FOUND,
) : AuthException(message, code)
