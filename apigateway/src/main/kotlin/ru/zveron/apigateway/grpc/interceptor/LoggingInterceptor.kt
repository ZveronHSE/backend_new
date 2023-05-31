package ru.zveron.apigateway.grpc.interceptor

import com.google.protobuf.GeneratedMessageV3
import io.grpc.ServerCall
import mu.KLogging
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor
import net.logstash.logback.marker.Markers
import net.logstash.logback.marker.Markers.append
import net.logstash.logback.marker.Markers.appendRaw
import ru.zveron.apigateway.utils.LogstashHelper.toJson
import ru.zveron.contract.apigateway.ApiGatewayRequest
import ru.zveron.library.grpc.interceptor.logging.LoggingServerInterceptor
import ru.zveron.library.grpc.interceptor.model.MethodType

@GrpcGlobalServerInterceptor
class LoggingInterceptor : LoggingServerInterceptor() {

    companion object : KLogging() {
        const val CALL_TYPE = "callType"
    }

    override fun <ReqT, RespT> logMessage(methodType: MethodType, call: ServerCall<ReqT, RespT>, message: Any) {
        if (methodType == MethodType.REQUEST) {
            val protoMessage = message as? ApiGatewayRequest

            protoMessage?.let {
                val marker = Markers.aggregate(
                    //todo sensitive masking
                    appendRaw("requestBody", protoMessage.requestBody.toStringUtf8()),
                    append("methodAlias", protoMessage.methodAlias),
                    append(CALL_TYPE, MethodType.REQUEST),
                )

                logger.info(marker) { }
            } ?: kotlin.run {
                message as GeneratedMessageV3
                logger.info(appendRaw("request", message.toJson())) { "Not an apigateway request" }
            }
        } else {
            logger.info { "send response" }
        }
    }
}
