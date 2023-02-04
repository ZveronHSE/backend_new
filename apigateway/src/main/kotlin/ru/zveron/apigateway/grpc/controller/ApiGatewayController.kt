package ru.zveron.apigateway.grpc.controller

import com.google.protobuf.kotlin.toByteStringUtf8
import com.google.protobuf.util.JsonFormat
import mu.KLogging
import net.devh.boot.grpc.server.service.GrpcService
import net.logstash.logback.marker.Markers.append
import ru.zveron.apigateway.grpc.ApiGatewayMapper.toServiceRequest
import ru.zveron.apigateway.grpc.service.ApiGatewayService
import ru.zveron.apigateway.utils.LogstashHelper.toJson
import ru.zveron.contract.apigateway.ApiGatewayRequest
import ru.zveron.contract.apigateway.ApigatewayResponse
import ru.zveron.contract.apigateway.ApigatewayServiceGrpcKt
import ru.zveron.contract.apigateway.apigatewayResponse

@GrpcService
class ApiGatewayController(
    private val service: ApiGatewayService,
) : ApigatewayServiceGrpcKt.ApigatewayServiceCoroutineImplBase() {

    companion object : KLogging()

    override suspend fun callApiGateway(request: ApiGatewayRequest): ApigatewayResponse {
        logger.debug(append("request", request.allFields.toJson())) { "Entered apigateway call processor" }

        val response = service.handleGatewayCall(request.toServiceRequest())

        logger.debug(append("response", response)) { "Service response" }
        return apigatewayResponse {
            this.responseBody = JsonFormat.printer().print(response).toByteStringUtf8()
        }
    }
}
