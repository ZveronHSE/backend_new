package ru.zv.authservice.exceptions

import io.grpc.Status

class WrongCodeException(message: String = "Wrong code entered", code: Status.Code = Status.Code.INVALID_ARGUMENT) :
    AuthException(message, code)
