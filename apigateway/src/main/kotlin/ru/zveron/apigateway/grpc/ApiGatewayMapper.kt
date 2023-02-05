package ru.zveron.apigateway.grpc

import ru.zveron.apigateway.component.model.ServiceScope
import ru.zveron.apigateway.grpc.service.dto.GatewayServiceRequest
import ru.zveron.apigateway.persistence.entity.AccessRole
import ru.zveron.contract.apigateway.ApiGatewayRequest

object ApiGatewayMapper {
    fun ApiGatewayRequest.toServiceRequest() = GatewayServiceRequest(this.methodAlias, this.requestBody.toStringUtf8())

    fun AccessRole.toScope() = when {
        this == AccessRole.ANY -> ServiceScope.ANY
        this == AccessRole.BUYER -> ServiceScope.BUYER
        else -> error("Unknown access role type")
    }
}