package ru.zveron.order.exception

import io.grpc.Metadata
import io.grpc.Status
import io.grpc.StatusRuntimeException

open class OrderException(status: Status = Status.INTERNAL, trailers: Metadata? = null, override val message: String) :
    StatusRuntimeException(status, trailers) {
}