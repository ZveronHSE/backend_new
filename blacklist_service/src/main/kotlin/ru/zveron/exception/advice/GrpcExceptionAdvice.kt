package ru.zveron.exception.advice

import io.grpc.Status
import net.devh.boot.grpc.server.advice.GrpcAdvice
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler
import ru.zveron.exception.BlacklistException

@GrpcAdvice
class GrpcExceptionAdvice {

    @GrpcExceptionHandler(BlacklistException::class)
    fun handleBlacklistException(e: BlacklistException) : Status = handleAnyException(e)

    @GrpcExceptionHandler
    fun handleAnyException(e: Exception) : Status {
        val description = e.message ?: "Something goes wrong"
        e.printStackTrace()
        return Status.INTERNAL.withDescription(description).withCause(e)
    }
}