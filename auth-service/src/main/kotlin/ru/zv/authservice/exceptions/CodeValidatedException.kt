package ru.zv.authservice.exceptions

import io.grpc.Status

class CodeValidatedException(
    message: String = "Code already validated",
    val code: Status.Code = Status.Code.PERMISSION_DENIED,
) :
    RuntimeException("Exception with message=$message and code=$code")
