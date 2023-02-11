package ru.zveron.authservice.grpc.client.model

import io.grpc.Metadata
import io.grpc.Status

sealed class ValidatePasswordResponse

object PasswordIsInvalid : ValidatePasswordResponse()

object PasswordIsValid : ValidatePasswordResponse()

data class PasswordValidationFailure(
    val message: String?,
    val status: Status,
    val metadata: Metadata?,
) : ValidatePasswordResponse()


object ValidatePasswordProfileNotFound : ValidatePasswordResponse()