package ru.zv.authservice.exceptions

import io.grpc.Status

class FingerprintException(message: String = "Wrong fingerprint", val code: Status.Code = Status.Code.PERMISSION_DENIED) :
    RuntimeException("Exception with message=$message and code=$code")
