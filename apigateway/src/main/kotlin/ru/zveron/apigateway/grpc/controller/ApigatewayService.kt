package ru.zveron.apigateway.grpc.controller

import mu.KLogging
import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.apigateway.grpc.service.ApiGatewayService
import ru.zveron.contract.apigateway.ApiGatewayRequest
import ru.zveron.contract.apigateway.ApigatewayResponse
import ru.zveron.contract.apigateway.ApigatewayServiceGrpcKt
import ru.zveron.contract.apigateway.apigatewayResponse


@GrpcService
class ApiGatewayGrpcController(
    private val service: ApiGatewayService,
) : ApigatewayServiceGrpcKt.ApigatewayServiceCoroutineImplBase() {

    companion object : KLogging()

    override suspend fun callApiGateway(request: ApiGatewayRequest): ApigatewayResponse {
        val serviceName = request.methodAlias.split(":")[0]
        val grpcService = request.methodAlias.split(":")[1]
        val grpcMethod = request.methodAlias.split(":")[2]

        val response =
            service.handleGatewayCall(serviceName, grpcService, grpcMethod, request.requestBody.toStringUtf8())

        return apigatewayResponse {
            this.responseBody = response.toByteString()
        }
    }
}
