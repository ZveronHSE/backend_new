package ru.zveron.exception

import io.grpc.Status

class ProfileNotFoundException(override val message: String) : ProfileException(message, Status.NOT_FOUND.code)
