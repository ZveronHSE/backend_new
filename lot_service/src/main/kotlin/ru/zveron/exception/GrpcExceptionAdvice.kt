package ru.zveron.exception

import io.grpc.Status
import mu.KLogging
import net.devh.boot.grpc.server.advice.GrpcAdvice
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler

@GrpcAdvice
class GrpcExceptionAdvice {
    companion object : KLogging()

    @GrpcExceptionHandler(LotException::class)
    fun handleLotExceptionException(ex: LotException): Status {
        logger.error(ex) { "Failed to handle request" }
        return Status.fromCode(ex.status.code ?: Status.Code.INTERNAL)
            .withDescription(ex.message)
    }

    @GrpcExceptionHandler
    fun handleAny(e: Exception): Status {
        logger.error { e.message }
        return Status.INTERNAL.withDescription("Exception: ${e.message}")
    }
}