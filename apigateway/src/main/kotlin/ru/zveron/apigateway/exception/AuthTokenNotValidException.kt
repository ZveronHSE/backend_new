package ru.zveron.apigateway.exception

import io.grpc.Metadata
import io.grpc.Status.Code

class AuthTokenNotValidException(
    message: String? = "Authentication token rejected",
    code: Code = Code.UNAUTHENTICATED,
    metadata: Metadata,
) : ApiGatewayException(message, code, metadata)
