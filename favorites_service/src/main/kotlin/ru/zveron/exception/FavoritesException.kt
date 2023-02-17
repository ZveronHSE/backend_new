package ru.zveron.exception

import io.grpc.Status
import java.lang.RuntimeException

class FavoritesException(message: String, val status: Status = Status.INVALID_ARGUMENT) : RuntimeException(message)