package ru.zveron.config

import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.kotlin.CoroutineContextServerInterceptor
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor
import kotlin.coroutines.CoroutineContext

@GrpcGlobalServerInterceptor
class HeaderInterceptor: CoroutineContextServerInterceptor() {
    companion object {
        private val profileIdKey = Metadata.Key.of("profile_id", Metadata.ASCII_STRING_MARSHALLER)
    }

    override fun coroutineContext(call: ServerCall<*, *>, headers: Metadata): CoroutineContext =
        AuthorizedProfileElement(headers.get(profileIdKey)?.toLong())
}