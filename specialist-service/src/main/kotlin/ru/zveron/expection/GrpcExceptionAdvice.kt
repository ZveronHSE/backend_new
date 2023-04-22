package ru.zveron.expection

import io.grpc.Status
import mu.KLogging
import net.devh.boot.grpc.server.advice.GrpcAdvice
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler

@GrpcAdvice
class GrpcExceptionAdvice {

    companion object : KLogging()

    @GrpcExceptionHandler(SpecialistException::class)
    fun handleEntityNotFound(e: SpecialistException): Status {
        logger.error(e.message, e)
        return e.code.withDescription(e.message)
    }

    @GrpcExceptionHandler
    fun handleAny(e: Exception): Status {
        logger.error(e.message, e)

        return Status.INTERNAL.withDescription("Exception: ${e.message}")
    }
}