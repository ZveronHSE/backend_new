package ru.zveron.apigateway.exception

class ApiGatewayException(message: String?, val code: String?) :
    RuntimeException(message = "Failed with code=$code; message=$message")
