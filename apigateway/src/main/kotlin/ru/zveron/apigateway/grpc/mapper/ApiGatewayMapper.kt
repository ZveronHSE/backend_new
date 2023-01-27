package ru.zveron.apigateway.grpc.mapper

import ru.zveron.apigateway.grpc.service.dto.GatewayServiceRequest
import ru.zveron.contract.apigateway.ApiGatewayRequest

object ApiGatewayMapper {
    fun ApiGatewayRequest.toServiceRequest() = GatewayServiceRequest(this.methodAlias, this.requestBody.toStringUtf8())
}
