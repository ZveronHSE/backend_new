package ru.zveron.service.application

import org.springframework.stereotype.Service
import ru.zveron.component.ChatPersistence
import ru.zveron.component.ConnectionStorage
import ru.zveron.model.dao.ChatRequestContext
import java.util.UUID

@Service
class ConnectionApplicationService(
    private val connectionStorage: ConnectionStorage,
    private val chatPersistence: ChatPersistence,
) {

    suspend fun registerConnection(nodeAddress: UUID, context: ChatRequestContext) {
        chatPersistence.registerConnection(nodeAddress, context)
        connectionStorage.registerConnection(context.authorizedProfileId, nodeAddress)
    }

    suspend fun closeConnection(nodeAddress: UUID, context: ChatRequestContext) {
        chatPersistence.closeConnection(context.authorizedProfileId)
        connectionStorage.closeConnection(context.authorizedProfileId, nodeAddress)
    }
}