package ru.zveron.apigateway.grpc.interceptor

import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.kotlin.CoroutineContextServerInterceptor
import mu.KLogging
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor
import ru.zveron.apigateway.grpc.context.AuthenticationContext
import kotlin.coroutines.CoroutineContext

@GrpcGlobalServerInterceptor
class AuthInterceptor : CoroutineContextServerInterceptor() {

    companion object : KLogging() {
        private val accessTokenKey = Metadata.Key.of("access_token", Metadata.ASCII_STRING_MARSHALLER)
    }

    override fun coroutineContext(call: ServerCall<*, *>, headers: Metadata): CoroutineContext {
        logger.info { "Entered coroutine context in auth interceptor, current service name=${call.methodDescriptor.serviceName}" }
        logger.info { "Headers=$headers" }
        headers.get(accessTokenKey).let {
            return AuthenticationContext(it ?: "")
        }
    }
}