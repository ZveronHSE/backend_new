package ru.zveron.authservice.grpc.interceptors

import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.kotlin.CoroutineContextServerInterceptor
import mu.KLogging
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor
import ru.zveron.authservice.grpc.context.AccessTokenElement
import kotlin.coroutines.CoroutineContext

@GrpcGlobalServerInterceptor
class AccessTokenInterceptor : CoroutineContextServerInterceptor() {

    companion object: KLogging() {
        val accessTokenKey = Metadata.Key.of("access_token", Metadata.ASCII_STRING_MARSHALLER)
    }

    override fun coroutineContext(call: ServerCall<*, *>, headers: Metadata): CoroutineContext {
        logger.info { headers }
        val accessTokenValue = headers.get(accessTokenKey)
        return AccessTokenElement(accessTokenValue)
    }
}