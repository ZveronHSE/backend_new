package ru.zveron.exception.advice

import io.grpc.Status
import mu.KLogging
import net.devh.boot.grpc.server.advice.GrpcAdvice
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler
import ru.zveron.exception.BlacklistException

@GrpcAdvice
class GrpcExceptionAdvice {

    companion object : KLogging()

    @GrpcExceptionHandler(BlacklistException::class)
    fun handleBlacklistException(e: BlacklistException): Status {
        logger.info { e.message }
        return Status.INVALID_ARGUMENT.withDescription(e.message).withCause(e)
    }

    @GrpcExceptionHandler
    fun handleAnyException(e: Exception): Status {
        val description = e.message ?: "Something goes wrong"
        logger.error { e }
        return Status.INTERNAL.withDescription(description).withCause(e)
    }
}
