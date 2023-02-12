package ru.zveron.exception

import io.grpc.Status

class ParameterException(
    override val message: String,
    val status: Status = Status.INVALID_ARGUMENT
) : RuntimeException(message)