package ru.zveron.authservice.config

import io.grpc.Status
import mu.KLogging
import net.devh.boot.grpc.server.advice.GrpcAdvice
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler

@GrpcAdvice
class AuthExceptionAdvice {

    companion object : KLogging()

    @GrpcExceptionHandler(ru.zveron.authservice.exception.AuthException::class)
    fun handleAuthException(e: ru.zveron.authservice.exception.AuthException): Status {
        logger.error { e.message }
        return Status.fromCode(e.code).withDescription(e.message)
    }

    @GrpcExceptionHandler
    fun handleAny(e: Exception): Status {
        logger.error { e.message }
        return Status.INTERNAL.withDescription("Exception: ${e.message}")
    }
}
