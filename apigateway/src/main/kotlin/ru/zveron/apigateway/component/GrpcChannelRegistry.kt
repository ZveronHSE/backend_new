package ru.zveron.apigateway.component

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import mu.KLogging
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Component
class GrpcChannelRegistry {

    companion object : KLogging() {
        const val DISCOVERY_PREFIX = "discovery:///"
    }

    private val channels = ConcurrentHashMap<String, ManagedChannel>()

    fun getChannel(service: String): ManagedChannel =
        channels["$DISCOVERY_PREFIX$service"] ?: createManagedChannel(service).also {
            channels["$DISCOVERY_PREFIX$service"] = it
        }

    private fun createManagedChannel(service: String) = ManagedChannelBuilder.forTarget("$DISCOVERY_PREFIX$service")
        .enableRetry()
        .idleTimeout(75, TimeUnit.SECONDS)
        .usePlaintext()
        .build()
}
