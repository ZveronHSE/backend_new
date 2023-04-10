package ru.zveron.exception

import io.grpc.Status
import ru.zveron.model.dao.ChatRequestContext

open class ChatException(val status: Status, message: String, val context: ChatRequestContext) :
    RuntimeException(message)