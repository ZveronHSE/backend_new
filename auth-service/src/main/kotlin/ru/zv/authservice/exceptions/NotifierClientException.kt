package ru.zv.authservice.exceptions

import io.grpc.Status

class NotifierClientException(
    message: String = "Something wrong with notifier client",
    code: Status.Code = Status.Code.UNAVAILABLE,
) : AuthException(message, code)
