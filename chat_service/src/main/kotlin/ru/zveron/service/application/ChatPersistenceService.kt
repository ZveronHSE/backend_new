package ru.zveron.service.application

import kotlinx.coroutines.channels.Channel
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.zveron.contract.chat.ChatRouteResponse
import java.util.*
import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentHashMap

@Service
class ChatPersistenceService {
    // TODO здесь будет использоваться кафка и таблица соединений

    @Value("\${service.persistence.connection.buffer-size}")
    var bufferSize: Int = 0

    companion object : KLogging()

    private val connections = ConcurrentHashMap<Long, Channel<ChatRouteResponse>>()

    fun registerConnection(nodeAddress: UUID, profileId: Long) {
        logger.info("Register connection with profile: $profileId on node: $nodeAddress")
        connections[profileId] = Channel(bufferSize)
    }

    fun getChannel(userId: Long): Channel<ChatRouteResponse>? {
        return connections[userId]
    }

    suspend fun sendMessageToConnection(profileId: Long, message: ChatRouteResponse) {
        logger.info("Send message to profile: $profileId")
        try {
            connections[profileId]?.send(message)
        } catch (e: CancellationException) {
            logger.info("Got CancellationException while sending message to profile: $profileId")
            connections.remove(profileId)
        }
    }

    fun closeConnection(profileId: Long) {
        logger.info("Close connection with profile: $profileId")
        connections.remove(profileId)?.close()
    }
}