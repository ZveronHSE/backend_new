package ru.zveron.service.presentation

import com.google.protobuf.Empty
import com.google.protobuf.StringValue
import com.google.protobuf.stringValue
import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.contract.chat.PingServiceExternalGrpcKt

@GrpcService
class PingServiceExternal : PingServiceExternalGrpcKt.PingServiceExternalCoroutineImplBase() {

    override suspend fun ping(request: Empty): StringValue = stringValue { value = "pong" }
}