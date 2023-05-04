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
            "Register connection {} {}",
            keyValue("profile-id", context.authorizedProfileId),
            keyValue("node-id", nodeAddress)
        )
        connections[context.authorizedProfileId] = Channel(bufferSize)
    }

    fun getChannel(profileId: Long): Channel<ChatRouteResponse>? {
        return connections[profileId]
    }

    suspend fun sendMessageToConnection(profileId: Long, message: ChatRouteResponse): Boolean {
        logger.debug("Send message to profile: $profileId")
        val channel = connections[profileId] ?: return false

        try {
            channel.send(message)
        } catch (e: CancellationException) {
            logger.debug(
                "Got CancellationException while sending message to profile: $profileId {}"
            )
            connections.remove(profileId)
        }

        return true
    }

    fun closeConnection(profileId: Long) {
        logger.debug("Close connection with profile: $profileId")
        connections.remove(profileId)?.close()
    }
}