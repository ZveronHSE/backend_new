package ru.zveron.order.exception

import io.grpc.Status

class ClientException(message: String, status: Status = Status.INTERNAL) :
    OrderException(status = status, message = message) {
}