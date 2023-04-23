package ru.zveron.exception

import io.grpc.Status
import mu.KLogging
import net.devh.boot.grpc.server.advice.GrpcAdvice
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler
import ru.zveron.library.grpc.exception.PlatformException

@GrpcAdvice
@Suppress("unused")
class GrpcExceptionAdvice {
    companion object : KLogging()


    @GrpcExceptionHandler(PlatformException::class)
    fun handlePlatformException(e: PlatformException): Status {
        logger.error(e) { "Platform exception" }

        return Status.fromCode(e.status.code).withDescription(e.message).withCause(e)
    }

    @GrpcExceptionHandler(ChatException::class)
    fun handleChatExceptionException(e: ChatException): Status {
        logger.error(e) { "Failed to handle request" }
        return Status.fromCode(e.status.code ?: Status.Code.INTERNAL)
            .withDescription(e.message)
    }

    @GrpcExceptionHandler
    fun handleAny(e: Exception): Status {
        logger.error(e) { "Unknown exception" }
        return Status.INTERNAL.withCause(e)
    }
}