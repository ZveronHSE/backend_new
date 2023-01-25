package ru.zv.authservice.exceptions

import io.grpc.Status

class WrongCodeException(message: String ="Wrong code entered", val code: Status.Code = Status.Code.INVALID_ARGUMENT) :
    RuntimeException("Exception with message=$message and code=$code")
