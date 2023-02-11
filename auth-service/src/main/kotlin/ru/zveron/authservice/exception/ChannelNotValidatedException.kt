package ru.zveron.authservice.exception

import io.grpc.Status

class ChannelNotValidatedException(
    message: String = "Communication channel for registration is not verified",
    code: Status.Code = Status.Code.FAILED_PRECONDITION,
) : AuthException(message, code)
