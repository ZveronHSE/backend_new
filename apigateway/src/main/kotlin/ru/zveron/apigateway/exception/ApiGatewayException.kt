package ru.zveron.apigateway.exception

import io.grpc.Metadata
import io.grpc.Status

open class ApiGatewayException(override val message: String?, val code: Status.Code?, val metadata: Metadata? = null) :
    RuntimeException("Failed with code=$code; message=$message")
