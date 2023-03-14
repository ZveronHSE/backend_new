package ru.zveron.exception

import io.grpc.Status
import mu.KLogging
import net.devh.boot.grpc.server.advice.GrpcAdvice
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler
import ru.zveron.library.grpc.exception.PlatformException

@GrpcAdvice
class GrpcExceptionAdvice {
    companion object : KLogging()


    @GrpcExceptionHandler(PlatformException::class)
    fun handlePlatformException(e: PlatformException): Status {
        logger.error { e }

        return Status.fromCode(e.status.code).withDescription(e.message).withCause(e)
    }

    @GrpcExceptionHandler(LotException::class)
    fun handleLotExceptionException(ex: LotException): Status {
        logger.error(ex) { "Failed to handle request: ${ex.message}" }
        return Status.fromCode(ex.status.code ?: Status.Code.INTERNAL)
            .withDescription(ex.message)
    }

    @GrpcExceptionHandler
    fun handleAny(e: Exception): Status {
        logger.error(e) { "Unknown exception: : ${e.message}" }
        return Status.INTERNAL.withCause(e).withDescription(e.message)
    }
}