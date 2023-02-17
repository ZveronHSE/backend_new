package ru.zveron.exception

import io.grpc.Status

class ProfileUnauthenticated(override val message: String) : ProfileException(message, Status.UNAUTHENTICATED.code)