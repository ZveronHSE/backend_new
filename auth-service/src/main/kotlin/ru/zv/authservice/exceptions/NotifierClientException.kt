package ru.zv.authservice.exceptions

import io.grpc.Status

class NotifierClientException(
    message: String = "Something wrong with notifier client",
    val code: Status.Code = Status.Code.UNAVAILABLE,
) :
    RuntimeException("Exception with message=$message and code=$code")
