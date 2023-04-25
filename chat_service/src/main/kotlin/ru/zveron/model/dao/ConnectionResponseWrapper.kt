package ru.zveron.model.dao

import ru.zveron.contract.chat.ChatRouteResponse

sealed class ChatRouteResponseWrapper

data class SingleConnectionResponse(
    val targetProfileId: Long,
    val responseBody: ChatRouteResponse,
) : ChatRouteResponseWrapper()

data class MultipleConnectionsResponse(
    val responses: Map<Long, ChatRouteResponse>,
) : ChatRouteResponseWrapper()

object NoneConnectionResponse : ChatRouteResponseWrapper()
