package ru.zveron.exception

import io.grpc.Status
import ru.zveron.model.dao.ChatRequestContext

class InvalidParamChatException(override val message: String, context: ChatRequestContext) :
    ChatException(Status.INVALID_ARGUMENT, message, context)