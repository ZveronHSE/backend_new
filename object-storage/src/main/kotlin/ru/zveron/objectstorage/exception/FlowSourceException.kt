package ru.zveron.objectstorage.exception

import io.grpc.Status

class FlowSourceException(flowSource: String) : ObjectStorageException(
    message = "Unknown flow source $flowSource",
    code = Status.Code.INVALID_ARGUMENT,
)
