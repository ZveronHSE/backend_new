package ru.zveron.exception

import io.grpc.Status

class ClientException(message: String? = null, status: Status = Status.INTERNAL) :
    OrderException(status = status, message = message ?: "no message provided in response") {

    companion object {
        fun notFound() = ClientException(message = "Entity not found by client", status = Status.NOT_FOUND)
    }
}