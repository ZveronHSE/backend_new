package ru.zveron.apigateway.grpc.interceptor

import brave.Tracer
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.kotlin.CoroutineContextServerInterceptor
import kotlinx.coroutines.slf4j.MDCContext
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor
import org.slf4j.MDC
import kotlin.coroutines.CoroutineContext


@GrpcGlobalServerInterceptor
class TracingInterceptor(
    private val tracer: Tracer,
) : CoroutineContextServerInterceptor() {
    override fun coroutineContext(call: ServerCall<*, *>, headers: Metadata): CoroutineContext {
        MDC.put("traceId", tracer.currentSpan().context().traceIdString())
        return MDCContext()
    }
}
