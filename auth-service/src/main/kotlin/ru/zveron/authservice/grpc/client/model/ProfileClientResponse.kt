package ru.zveron.authservice.grpc.client.model

import io.grpc.Metadata
import io.grpc.Status

sealed class ProfileClientResponse

data class ProfileFound(
    val id: Long,
    val name: String,
    val surname: String,
) : ProfileClientResponse()

object ProfileNotFound : ProfileClientResponse()

data class ProfileUnknownFailure(
    val message: String?,
    val code: Status.Code,
    val metadata: Metadata,
) : ProfileClientResponse()
