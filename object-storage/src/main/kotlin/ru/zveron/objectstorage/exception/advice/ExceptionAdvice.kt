package ru.zveron.objectstorage.exception.advice

import io.grpc.Status
import io.grpc.StatusException
import mu.KLogging
import net.devh.boot.grpc.server.advice.GrpcAdvice
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler
import net.logstash.logback.marker.Markers
import ru.zveron.objectstorage.exception.ObjectStorageException

@Suppress("unused")
@GrpcAdvice
class ExceptionAdvice {

    companion object : KLogging()

    @GrpcExceptionHandler(Exception::class)
    fun handleAnyException(ex: Exception): Status? {
        logger.error(ex) { "Image storage request failed with unhandled exception" }

        return Status.fromCode(Status.Code.INTERNAL).withDescription(ex.message)
    }

    @GrpcExceptionHandler(StatusException::class)
    fun handleGrpcException(ex: StatusException): Status? {
        logger.error(ex) { "Image storage request failed with unhandled exception for rpc call" }

        return Status.fromCode(ex.status.code ?: Status.Code.INTERNAL).withDescription(ex.message)
    }

    @GrpcExceptionHandler(ObjectStorageException::class)
    fun handleServiceException(ex: ObjectStorageException): Status? {
        logger.error(Markers.append("metadata", ex.metadata), ex) { "Image storage request failed with exception" }

        return Status.fromCode(ex.code).withDescription(ex.message)
    }
}
