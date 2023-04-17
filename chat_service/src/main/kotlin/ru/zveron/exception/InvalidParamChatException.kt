package ru.zveron.exception

import io.grpc.Status

class InvalidParamChatException(override val message: String) : ChatException(Status.INVALID_ARGUMENT, message)