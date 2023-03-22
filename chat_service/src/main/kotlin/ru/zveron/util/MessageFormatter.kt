package ru.zveron.util

import ru.zveron.model.dao.ChatRequestContext

object MessageFormatter {

    fun String.appendContext(context: ChatRequestContext): String =
        "$this Connection-id: ${context.connectionId} user-id: ${context.authorizedProfileId}"
}