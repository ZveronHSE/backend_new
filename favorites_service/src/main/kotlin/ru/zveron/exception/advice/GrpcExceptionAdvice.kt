package ru.zveron.exception.advice

import io.grpc.Status
import mu.KLogging
import net.devh.boot.grpc.server.advice.GrpcAdvice
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler
import ru.zveron.exception.FavoritesException
import ru.zveron.library.grpc.exception.PlatformException

@GrpcAdvice
class GrpcExceptionAdvice {

    companion object : KLogging()


    @GrpcExceptionHandler(PlatformException::class)
    fun handlePlatformException(e: PlatformException): Status {
        logger.error { e }

        return Status.fromCode(e.status.code).withDescription(e.message).withCause(e)
    }

    @GrpcExceptionHandler(FavoritesException::class)
    fun handleFavoritesException(e: FavoritesException): Status {
        logger.info { e.message }
        return e.status.withDescription(e.message).withCause(e)
    }

    @GrpcExceptionHandler
    fun handleAnyException(e: Exception): Status {
        val description = e.message ?: "Something goes wrong"
        logger.error { e }
        return Status.INTERNAL.withDescription(description).withCause(e)
    }
}