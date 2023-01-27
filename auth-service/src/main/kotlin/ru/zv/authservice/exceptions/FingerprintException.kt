package ru.zv.authservice.exceptions

import io.grpc.Status

class FingerprintException(
    message: String = "Wrong fingerprint",
    code: Status.Code = Status.Code.PERMISSION_DENIED,
) : AuthException(message, code)
