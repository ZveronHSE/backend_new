package ru.zveron.objectstorage.exception

import io.grpc.Metadata
import io.grpc.Status

open class ObjectStorageException(message: String? = null, val code: Status.Code, val metadata: Metadata? = null) :
    RuntimeException(message)