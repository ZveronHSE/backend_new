package ru.zveron.exception

import io.grpc.Status

class CategoryException(
    val status: Status,
    override val message: String
) : RuntimeException(message)
