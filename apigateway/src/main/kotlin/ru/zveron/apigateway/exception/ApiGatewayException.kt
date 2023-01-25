package ru.zveron.apigateway.exception

import io.grpc.Status

class ApiGatewayException(override val message: String?, val code: Status.Code?) :
    RuntimeException("Failed with code=$code; message=$message")
