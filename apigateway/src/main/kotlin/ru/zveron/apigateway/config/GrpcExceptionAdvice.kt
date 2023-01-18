package ru.zveron.apigateway.config

import com.google.protobuf.value
import io.grpc.Status
import mu.KLogging
import net.devh.boot.grpc.server.advice.GrpcAdvice
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler

@GrpcAdvice
@Suppress("unused")
class GrpcExceptionAdvice {

    companion object : KLogging()

    @GrpcExceptionHandler
    fun handleAnyException(e: Exception): Status {
        val description = e.message ?: "Something goes wrong"
        logger.error { e }
        return Status.INTERNAL.withDescription(description).withCause(e)
    }
}
