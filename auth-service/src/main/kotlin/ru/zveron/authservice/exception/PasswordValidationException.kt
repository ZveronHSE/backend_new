package ru.zveron.authservice.exception

import io.grpc.Metadata
import io.grpc.Status

class PasswordValidationException(
    message: String? = "Password validation failed",
    code: Status.Code = Status.Code.UNAUTHENTICATED,
    metadata: Metadata? = null,
) : AuthException(message, code, metadata)