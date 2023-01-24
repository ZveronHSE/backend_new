package ru.zv.authservice.exceptions

import io.grpc.Status

class FingerprintException(message: String?, val code: Status.Code) :
    RuntimeException("Exception with message=$message and code=$code")
