package ru.zveron.authservice.exception

import io.grpc.Status

class SocialMediaException(
    message: String? = "Social media request failed",
    code: Status.Code = Status.Code.INTERNAL,
) : AuthException(message, code)