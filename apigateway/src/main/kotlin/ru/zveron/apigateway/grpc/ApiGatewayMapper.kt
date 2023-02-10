package ru.zveron.apigateway.grpc

import ru.zveron.apigateway.component.constant.ServiceScope
import ru.zveron.apigateway.grpc.service.dto.GatewayServiceRequest
import ru.zveron.apigateway.persistence.constant.AccessScope
import ru.zveron.contract.apigateway.ApiGatewayRequest

object ApiGatewayMapper {
    fun ApiGatewayRequest.toServiceRequest() = GatewayServiceRequest(this.methodAlias, this.requestBody.toStringUtf8())

    fun AccessScope.toScope() = when(this) {
        AccessScope.ANY -> ServiceScope.ANY
        AccessScope.BUYER -> ServiceScope.BUYER
        else -> error("Unknown access role type")
    }
}