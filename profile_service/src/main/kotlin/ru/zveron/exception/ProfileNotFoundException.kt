package ru.zveron.exception

import io.grpc.Status

class ProfileNotFoundException(override val message: String, code: Status.Code) : ProfileException(message, code)