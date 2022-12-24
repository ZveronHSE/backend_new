package ru.zveron.exception

import io.grpc.Status
import mu.KLogging
import net.devh.boot.grpc.server.advice.GrpcAdvice
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler
import javax.persistence.EntityNotFoundException

@GrpcAdvice
class GrpcExceptionAdvice {

    companion object : KLogging()

    @GrpcExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFound(e: EntityNotFoundException): Status {
        logger.error { e.message }
        return Status.NOT_FOUND.withDescription("${e.message}")
    }

    @GrpcExceptionHandler
    fun handleAny(e: Exception): Status {
        logger.error { e.message }
        return Status.INTERNAL.withDescription("Exception: ${e.message}")
    }
}