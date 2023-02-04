package ru.zveron.apigateway.grpc.interceptor

import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.kotlin.CoroutineContextServerInterceptor
import mu.KLogging
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor
import net.logstash.logback.marker.Markers.append
import ru.zveron.apigateway.grpc.context.AuthenticationContext
import ru.zveron.apigateway.utils.LogstashHelper.toMarker
import kotlin.coroutines.CoroutineContext

@GrpcGlobalServerInterceptor
class AuthInterceptor : CoroutineContextServerInterceptor() {

    companion object : KLogging() {
        private val accessTokenKey = Metadata.Key.of("access_token", Metadata.ASCII_STRING_MARSHALLER)
    }

    override fun coroutineContext(call: ServerCall<*, *>, headers: Metadata): CoroutineContext {
        logger.debug(append("headers", headers.toMarker())) { "Entered coroutine context in auth interceptor" }
        headers.get(accessTokenKey).let {
            logger.debug(append("accessToken", it)) { "Access token in interceptor" }
            return AuthenticationContext(it ?: "")
        }
    }
}
