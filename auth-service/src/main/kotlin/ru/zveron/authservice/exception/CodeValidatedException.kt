package ru.zveron.authservice.exception

import io.grpc.Status

class CodeValidatedException(
    message: String = "Code already validated, cannot be reused",
    code: Status.Code = Status.Code.PERMISSION_DENIED,
) : AuthException(message, code)
