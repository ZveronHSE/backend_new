package ru.zveron.interceptor

import com.datastax.oss.driver.api.core.uuid.Uuids
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.kotlin.CoroutineContextServerInterceptor
import kotlinx.coroutines.slf4j.MDCContext
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor
import kotlin.coroutines.CoroutineContext

@GrpcGlobalServerInterceptor
class ConnectionInterceptor: CoroutineContextServerInterceptor() {

    override fun coroutineContext(call: ServerCall<*, *>, headers: Metadata): CoroutineContext {
        return MDCContext(mapOf("connection-id" to Uuids.timeBased().toString()))
    }

}