package ru.zveron.exception

import io.grpc.Status
import mu.KLogging
import net.devh.boot.grpc.server.advice.GrpcAdvice
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler

@GrpcAdvice
class GrpcExceptionAdvice {

    companion object : KLogging()

    @GrpcExceptionHandler
    fun handleAny(e: Exception): Status {
        logger.error { e.message }

        return Status.INTERNAL.withDescription("Exception: ${e.message}").withCause(e)
    }

    @GrpcExceptionHandler(CategoryException::class)
    fun handleCategoryException(e: CategoryException): Status {
        logger.error { e.message }

        return e.status.withDescription(e.message)
    }
}
