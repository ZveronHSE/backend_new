package ru.zveron.authservice.exception

import io.grpc.Status

class CodeValidatedException(
    message: String = "Code already validated",
    code: Status.Code = Status.Code.PERMISSION_DENIED,
) : AuthException(message, code)
