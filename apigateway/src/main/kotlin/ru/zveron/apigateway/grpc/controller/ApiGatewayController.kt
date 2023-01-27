package ru.zveron.apigateway.grpc.controller

import com.google.protobuf.kotlin.toByteStringUtf8
import mu.KLogging
import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.apigateway.grpc.mapper.ApiGatewayMapper.toServiceRequest
import ru.zveron.apigateway.grpc.service.ApiGatewayService
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
        logger.debug { "Apigw entrypoint, request=$request" }

        val response = service.handleGatewayCall(request.toServiceRequest())

        logger.debug { "Apigw entrypoint, response $response" }

        return apigatewayResponse {
            this.responseBody = response.toString().toByteStringUtf8()
        }
    }
}
