package ru.zveron.authservice.grpc.client.model

import io.grpc.Metadata
import io.grpc.Status

sealed class RegisterProfileResponse
object RegisterProfileAlreadyExists : RegisterProfileResponse()
data class RegisterProfileFailure(
    val message: String? = "Unknown failure encountered",
    val code: Status,
    val metadata: Metadata? = null,
) : RegisterProfileResponse()

data class RegisterProfileSuccess(
    val profileId: Long,
) : RegisterProfileResponse()