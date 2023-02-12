package ru.zveron.apigateway.grpc.client

import io.grpc.Metadata
import io.grpc.Status

sealed class GrpcAuthClientResponse
data class AccessTokenNotValid(
    val message: String?,
    val code: Status.Code,
    val metadata: Metadata,
) : GrpcAuthClientResponse()

data class AccessTokenUnknown(
    val message: String?,
    val code: Status.Code,
    val metadata: Metadata,
) : GrpcAuthClientResponse()

data class AccessTokenValid(
    val profileId: Long,
) : GrpcAuthClientResponse()
