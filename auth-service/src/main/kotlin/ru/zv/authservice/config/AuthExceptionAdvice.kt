package ru.zv.authservice.config

import io.grpc.Status
import mu.KLogging
import net.devh.boot.grpc.server.advice.GrpcAdvice
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler
import ru.zv.authservice.exceptions.AuthException

@GrpcAdvice
class AuthExceptionAdvice {

    companion object : KLogging()

    @GrpcExceptionHandler(AuthException::class)
    fun handleAuthException(e: AuthException): Status {
        logger.error { e.message }
        return Status.fromCode(e.code).withDescription(e.message).withCause(e)
    }

    @GrpcExceptionHandler
    fun handleAny(e: Exception): Status {
        logger.error { e.message }
        return Status.INTERNAL.withDescription("Exception: ${e.message}")
    }
}
