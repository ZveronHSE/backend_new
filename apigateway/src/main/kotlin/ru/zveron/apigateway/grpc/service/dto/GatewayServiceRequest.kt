package ru.zveron.apigateway.grpc.service.dto

data class GatewayServiceRequest(
    val alias: String,
    val requestBody: String,
)
