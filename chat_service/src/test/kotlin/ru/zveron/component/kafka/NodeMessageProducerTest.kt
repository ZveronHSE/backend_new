package ru.zveron.component.kafka

import app.cash.turbine.testIn
import com.datastax.oss.driver.api.core.uuid.Uuids
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.ChatServiceApplicationTest
import ru.zveron.common.generator.PrimitivesGenerator.generateLongs
import ru.zveron.common.generator.PrimitivesGenerator.generateString
import ru.zveron.component.ChatPersistence
import ru.zveron.component.ConnectionStorage
import ru.zveron.contract.chat.chatRouteResponse
import ru.zveron.contract.chat.model.NoPayloadEventType
import ru.zveron.contract.chat.model.noPayloadEvent
import ru.zveron.contract.chat.receiveEvent
import ru.zveron.library.grpc.interceptor.model.MetadataElement
import ru.zveron.library.grpc.model.Metadata
import ru.zveron.model.dao.ChatRequestContext

class NodeMessageProducerTest: ChatServiceApplicationTest() {

    @Autowired
    lateinit var connectionStorage: ConnectionStorage

    @Autowired
    lateinit var chatPersistence: ChatPersistence

    @Autowired
    lateinit var nodeMessageProducer: NodeMessageProducer

    @Test
    fun `sendMessage when open connection on current node`() {
        val nodeId = Uuids.timeBased()
        val (authorizedProfileId, profileId) = generateLongs(2)
        val requestContext = ChatRequestContext(profileId)
        val chatId = generateString(10)
        val message = chatRouteResponse { receiveEvent = receiveEvent {
            this.chatId = chatId
            noPayloadEvent = noPayloadEvent { type = NoPayloadEventType.TEXTING }
        } }
        val backgroundScope = CoroutineScope(MetadataElement(Metadata(authorizedProfileId)))

        chatPersistence.registerConnection(nodeId, requestContext)
        val responseFlow = chatPersistence.getChannel(profileId)!!.receiveAsFlow()
        val testedFlow = responseFlow.testIn(backgroundScope)

        runBlocking {
            nodeMessageProducer.sendMessage(profileId, message)

            testedFlow.awaitItem().receiveEvent.apply {
                this.chatId shouldBe chatId
                this.noPayloadEvent.type shouldBe NoPayloadEventType.TEXTING
            }
        }
    }

    @Test
    fun `sendMessage when no open connections in any node`() {
        val nodeId = Uuids.timeBased()
        val (authorizedProfileId, profileId) = generateLongs(2)
        val requestContext = ChatRequestContext(profileId)
        val chatId = generateString(10)
        val message = chatRouteResponse { receiveEvent = receiveEvent {
            this.chatId = chatId
            noPayloadEvent = noPayloadEvent { type = NoPayloadEventType.TEXTING }
        } }
        val backgroundScope = CoroutineScope(MetadataElement(Metadata(authorizedProfileId)))

        chatPersistence.registerConnection(nodeId, requestContext)
        val responseFlow = chatPersistence.getChannel(profileId)!!.receiveAsFlow()
        val testedFlow = responseFlow.testIn(backgroundScope)

        runBlocking {
            connectionStorage.registerConnection(profileId, nodeId)
            connectionStorage.closeConnection(profileId, nodeId)
            chatPersistence.closeConnection(profileId)
            nodeMessageProducer.sendMessage(profileId, message)

            testedFlow.awaitComplete()
        }
    }
}