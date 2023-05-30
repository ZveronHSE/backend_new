package ru.zveron.exception

import io.grpc.Status

class OrderNotFoundException(id: Long) :
    OrderException(message = "Order with id $id not found", status = Status.NOT_FOUND)
