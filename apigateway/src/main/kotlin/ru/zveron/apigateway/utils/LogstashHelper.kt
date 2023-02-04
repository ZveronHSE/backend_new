package ru.zveron.apigateway.utils

import com.google.gson.Gson
import io.grpc.Metadata
import io.grpc.Metadata.ASCII_STRING_MARSHALLER
import net.logstash.logback.marker.LogstashMarker
import net.logstash.logback.marker.Markers

object LogstashHelper {
    val mapper: Gson = Gson().newBuilder().setPrettyPrinting().create()

    fun Any.toJson(): String? {
        return mapper.toJson(this)
    }

    fun Metadata.toMarker(): LogstashMarker? {
        val keys = this.keys()
        val marker = Markers.empty()
        keys.forEach {
            marker.add(Markers.append(it, this[Metadata.Key.of(it, ASCII_STRING_MARSHALLER)]))
        }
        return marker
    }
}