package ru.zv.authservice.exceptions

import io.grpc.Status

class NotifierClientException(message: String?, val code: Status.Code) :
    RuntimeException("Exception with message=$message and code=$code")
