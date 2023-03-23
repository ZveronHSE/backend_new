package ru.zveron.apigateway.utils

import com.google.protobuf.DynamicMessage
import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.util.JsonFormat

object LogstashHelper {
    private val protoJsonPrinter = JsonFormat.printer().omittingInsignificantWhitespace()

    fun DynamicMessage.toJson(): String = protoJsonPrinter.print(this)

    fun <T : GeneratedMessageV3> T.toJson(): String = protoJsonPrinter.print(this)
}
