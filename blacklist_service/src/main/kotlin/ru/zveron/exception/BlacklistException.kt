package ru.zveron.exception

import io.grpc.Status

class BlacklistException(message: String, val status: Status = Status.INVALID_ARGUMENT) : RuntimeException(message)
