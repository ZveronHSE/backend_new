package ru.zveron.exception

import io.grpc.Status

class LotException(val status: Status, message: String) : RuntimeException(message)