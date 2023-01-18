package ru.zveron.apigateway.exception.advice

import io.grpc.Status
import mu.KLogging
import net.devh.boot.grpc.server.advice.GrpcAdvice
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler
import ru.zveron.apigateway.exception.ApiGatewayException

@GrpcAdvice
@Suppress("unused")
class ApiGatewayExceptionAdvice {

    companion object : KLogging()

    @GrpcExceptionHandler(ApiGatewayException::class)
    fun handleApiGatewayException(ex: ApiGatewayException): Status {
        logger.error(ex) { "Failed to handle request" }
        return Status.fromCode(ex.code
            ?.let { Status.Code.valueOf(it) } ?: Status.Code.INTERNAL)
            .withDescription(ex.message)
    }

    @GrpcExceptionHandler(Exception::class)
    fun handleAnyException(ex: Exception): Status {
        logger.error(ex) { "Unknown exception" }
        return Status.INTERNAL.withCause(ex)
    }
}
