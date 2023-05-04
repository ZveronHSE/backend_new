package ru.zveron.component.kafka

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import mu.KLogging
import net.logstash.logback.argument.StructuredArguments.keyValue
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component
import ru.zveron.component.ChatPersistence
import ru.zveron.component.ConnectionStorage
import ru.zveron.contract.chat.ChatRouteResponse
import java.util.UUID

@Component
class NodeMessageConsumer(
    private val chatPersistence: ChatPersistence,
    private val connectionStorage: ConnectionStorage,
    private val nodeMessageProducer: NodeMessageProducer,
) {

    @Value("#{chatConfigBean.nodeUuid}")
    lateinit var instanceId: UUID

    companion object : KLogging()

    @KafkaListener(
        topics = ["#{chatConfigBean.nodeUuidFormatted}"],
        groupId = "\${spring.kafka.consumer.group-id}",
        containerFactory = "listenerFactory",
    )
    fun listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) key: Long?, message: ChatRouteResponse) {
        if (key == null) {
            logger.error("Receive message without key")
            return
        }
        logger.info("Receive message for profile with id: $key")

        runBlocking {
            if (!chatPersistence.sendMessageToConnection(key, message)) {
                logger.debug(
                    "No active connections with profile {} on current node. Forwarding message",
                    keyValue("profile-id", key.toString())
                )

                supervisorScope {
                    launch { connectionStorage.closeConnection(key, instanceId) }
                    launch { nodeMessageProducer.forwardMessage(key, message) }
                }
            }
        }
    }
}