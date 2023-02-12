package ru.zveron.authservice.exception

import io.grpc.Metadata
import io.grpc.Status

class RegistrationException(
    message: String? = "Account registration failed",
    code: Status.Code = Status.Code.INTERNAL,
    metadata: Metadata? = null
) : AuthException(message, code, metadata)
