package ru.zveron.review.exception.advice

import io.grpc.Status
import mu.KLogging
import net.devh.boot.grpc.server.advice.GrpcAdvice
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler

@GrpcAdvice
@Suppress("unused")
class ExceptionAdvice {

    companion object : KLogging()

//    @GrpcExceptionHandler(OrderException::class)
//    fun handleBusinessException(ex: OrderException): Status {
//        logger.error("Failed to handle request", ex)
//
//        return Status.fromCode(ex.status.code ?: Status.Code.INTERNAL)
//            .withDescription(ex.message)
//    }

    @GrpcExceptionHandler(Exception::class)
    fun handleAnyException(ex: Exception): Status {
        logger.error("Unknown exception occurred", ex)

        return Status.INTERNAL.withCause(ex).withDescription(ex.message)
    }
}
