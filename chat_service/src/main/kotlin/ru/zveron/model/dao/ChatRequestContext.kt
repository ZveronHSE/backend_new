package ru.zveron.model.dao

import java.util.*

data class ChatRequestContext(
    val connectionId: UUID,
    val authorizedProfileId: Long,
)