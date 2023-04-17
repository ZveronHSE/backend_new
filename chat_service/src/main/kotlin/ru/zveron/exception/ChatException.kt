package ru.zveron.exception

import io.grpc.Status

open class ChatException(val status: Status, message: String) : RuntimeException(message)