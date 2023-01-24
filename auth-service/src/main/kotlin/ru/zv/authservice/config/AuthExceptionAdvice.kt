package ru.zv.authservice.config

import io.grpc.Status
import mu.KLogging
import net.devh.boot.grpc.server.advice.GrpcAdvice
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler
import ru.zv.authservice.exceptions.AuthException
import ru.zv.authservice.exceptions.FingerprintException
import ru.zv.authservice.exceptions.NotifierClientException
import ru.zv.authservice.exceptions.WrongCodeException

@GrpcAdvice
class AuthExceptionAdvice {

    companion object : KLogging()

    @GrpcExceptionHandler(AuthException::class)
    fun handleEntityNotFound(e: AuthException): Status {
        logger.error { e.message }
        return Status.fromCode(e.code).withDescription(e.message)
    }

    @GrpcExceptionHandler(NotifierClientException::class)
    fun handleNotifierException(e: NotifierClientException): Status {
        logger.error { e.message }
        return Status.fromCode(e.code).withDescription(e.message)
    }

    @GrpcExceptionHandler(WrongCodeException::class)
    fun handleWrongCode(e: WrongCodeException): Status {
        logger.error { e.message }
        return Status.fromCode(e.code).withDescription(e.message)
    }

    @GrpcExceptionHandler(FingerprintException::class)
    fun handleFingerprintException(e: FingerprintException): Status {
        logger.error { e.message }
        return Status.fromCode(e.code).withDescription(e.message)
    }

    @GrpcExceptionHandler
    fun handleAny(e: Exception): Status {
        logger.error { e.message }
        return Status.INTERNAL.withDescription("Exception: ${e.message}")
    }
}