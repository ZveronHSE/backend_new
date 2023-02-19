package ru.zveron.authservice.grpc.client.model

import io.grpc.Metadata
import io.grpc.Status

sealed class FindProfileResponse

data class ProfileFound(
    val id: Long,
    val name: String,
    val surname: String,
) : FindProfileResponse()

object ProfileNotFound : FindProfileResponse()

data class FindProfileUnknownFailure(
    val message: String?,
    val code: Status.Code,
    val metadata: Metadata,
) : FindProfileResponse()
