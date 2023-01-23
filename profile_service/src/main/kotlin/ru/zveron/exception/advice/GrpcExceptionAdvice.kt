package ru.zveron.exception.advice

import io.grpc.Status
import mu.KLogging
import net.devh.boot.grpc.server.advice.GrpcAdvice
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler
import ru.zveron.exception.ProfileException
import ru.zveron.exception.ProfileNotFoundException

@GrpcAdvice
class GrpcExceptionAdvice {

    companion object : KLogging()

    @GrpcExceptionHandler(ProfileNotFoundException::class)
    fun handleProfileException(e: ProfileNotFoundException) : Status {
        logger.info {e.message}
        return Status.NOT_FOUND.withDescription(e.message).withCause(e)
    }

    @GrpcExceptionHandler(ProfileException::class)
    fun handleProfileException(e: ProfileException) : Status {
        logger.info {e.message}
        return Status.INVALID_ARGUMENT.withDescription(e.message).withCause(e)
    }

    @GrpcExceptionHandler
    fun handleAnyException(e: Exception) : Status {
        val description = e.message ?: "Something goes wrong"
        logger.error { e }
        return Status.INTERNAL.withDescription(description).withCause(e)
    }
}