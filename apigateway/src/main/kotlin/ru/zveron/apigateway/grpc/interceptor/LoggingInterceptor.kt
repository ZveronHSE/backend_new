package ru.zveron.apigateway.grpc.interceptor

import brave.Tracer
import com.google.protobuf.GeneratedMessageV3
import io.grpc.ForwardingServerCall
import io.grpc.ForwardingServerCallListener
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import mu.KLogging
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor
import net.logstash.logback.marker.Markers
import net.logstash.logback.marker.Markers.append
import net.logstash.logback.marker.Markers.appendRaw
import org.apache.commons.lang3.StringUtils
import ru.zveron.apigateway.utils.LogstashHelper.toJson
import ru.zveron.contract.apigateway.ApiGatewayRequest
import ru.zveron.contract.apigateway.ApigatewayResponse
import ru.zveron.library.grpc.interceptor.model.MethodType

@GrpcGlobalServerInterceptor
class LoggingInterceptor(
    private val tracer: Tracer,
) : ServerInterceptor {

    companion object : KLogging() {
        const val CALL_TYPE = "callType"
        private val TRACE_ID_KEY = Metadata.Key.of("x-zv-trace-id", Metadata.ASCII_STRING_MARSHALLER)
    }

    final override fun <ReqT : Any, RespT : Any> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata?,
        next: ServerCallHandler<ReqT, RespT>,
    ): ServerCall.Listener<ReqT> {
        val traceId = tracer.currentSpan().context().traceIdString()
        val wrappedCall = object : ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
            override fun sendMessage(message: RespT) {
                logResponse(message)
                super.sendMessage(message)
            }

            override fun sendHeaders(headers: Metadata?) {
                super.sendHeaders(headers.apply {
                    //todo: fix it somehow or move to an appropriate place. Will do for now to pass trace in response
                    this?.put(TRACE_ID_KEY, traceId)
                })
            }
        }

        return object : ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
            next.startCall(
                wrappedCall,
                headers,
            )
        ) {
            override fun onMessage(message: ReqT) {
                logRequest(message)
                super.onMessage(message)
            }
        }
    }

    private fun <ReqT> logRequest(message: ReqT) {
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
    }

    private fun <RespT> logResponse(message: RespT) {
        val response = message as? ApigatewayResponse

        response?.let {
            response.responseBody.toStringUtf8().let { StringUtils.normalizeSpace(it) }
            //todo sensitive masking
            val marker = Markers.aggregate(
                appendRaw("responseBody", response.toJson()),
                append(CALL_TYPE, MethodType.RESPONSE),
            )
            logger.info(marker) { }
        } ?: kotlin.run {
            message as GeneratedMessageV3
            logger.info(append("response", message.toJson())) { "Not an apigateway response" }
        }
    }
}
