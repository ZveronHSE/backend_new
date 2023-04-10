package ru.zveron.component

import kotlinx.coroutines.channels.Channel
import mu.KLogging
import net.logstash.logback.argument.StructuredArguments.keyValue
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.zveron.contract.chat.ChatRouteResponse
import ru.zveron.model.dao.ChatRequestContext
import java.util.*
import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentHashMap

@Service
class ChatPersistence {
    // TODO здесь будет использоваться кафка и таблица соединений

    @Value("\${service.persistence.connection.buffer-size}")
    var bufferSize: Int = 0

    companion object : KLogging()

    private val connections = ConcurrentHashMap<Long, Channel<ChatRouteResponse>>()

    fun registerConnection(nodeAddress: UUID, context: ChatRequestContext) {
        logger.debug(
            "Register connection {} {} {}",
            keyValue("profile-id", context.authorizedProfileId),
            keyValue("connection-id", context.connectionId),
            keyValue("node-id", nodeAddress)
        )
        connections[context.authorizedProfileId] = Channel(bufferSize)
    }

    fun getChannel(userId: Long): Channel<ChatRouteResponse>? {
        return connections[userId]
    }

    suspend fun sendMessageToConnection(profileId: Long, message: ChatRouteResponse, context: ChatRequestContext) {
        logger.debug("Send message to profile: $profileId {}", keyValue("connection-id", context.connectionId))
        try {
            connections[profileId]?.send(message)
        } catch (e: CancellationException) {
            logger.debug(
                "Got CancellationException while sending message to profile: $profileId {}",
                keyValue("connection-id", context.connectionId)
            )
            connections.remove(profileId)
        }
    }

    fun closeConnection(profileId: Long, context: ChatRequestContext) {
        logger.debug("Close connection with profile: $profileId {}", keyValue("connection-id", context.connectionId))
        connections.remove(profileId)?.close()
    }
}