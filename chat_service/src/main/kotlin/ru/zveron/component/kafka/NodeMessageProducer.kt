package ru.zveron.component.kafka

import mu.KLogging
import net.logstash.logback.argument.StructuredArguments.keyValue
import org.slf4j.MDC.put
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import ru.zveron.component.ChatPersistence
import ru.zveron.component.ConnectionStorage
import ru.zveron.contract.chat.ChatRouteResponse

@Component
class NodeMessageProducer(
    private val chatPersistence: ChatPersistence,
    private val connectionStorage: ConnectionStorage,
    private val kafkaTemplate: KafkaTemplate<Long, ChatRouteResponse>,
) {

    companion object: KLogging()

    suspend fun sendMessage(profileId: Long, message: ChatRouteResponse) {
        if (chatPersistence.sendMessageToConnection(profileId, message)) {
            return
        }

        forwardMessage(profileId, message)
    }

    suspend fun forwardMessage(profileId: Long, message: ChatRouteResponse) {
        val connection = connectionStorage.getActiveConnectionWithNewestStatusChange(profileId)
        if (connection == null) {
            logger.debug("No active connections with profile {}", keyValue("profile-id", profileId.toString()))
            return
        }

        send(connection.nodeAddress.toString(), profileId, message)
    }

    private fun send(connectionId: String, profileId: Long, message: ChatRouteResponse) {
        logger.debug("Send message to instance with id: $connectionId", put("profile-id", profileId.toString()))
        kafkaTemplate.send(connectionId, profileId, message)
    }
}