package ru.zveron.component

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component
import ru.zveron.model.entity.Connection
import ru.zveron.repository.ConnectionRepository
import java.util.UUID

@Component
class ConnectionStorage(val connectionRepository: ConnectionRepository) {

    suspend fun getConnectionWithNewestStatusChange(profileId: Long) = connectionRepository
        .findAllOpenConnectionsByProfile(profileId)
        .toList()
        .maxByOrNull { it.lastStatusChange }

    suspend fun getActiveConnectionWithLatestStatusChange(profileId: Long) = connectionRepository
        .findAllOpenConnectionsByProfile(profileId)
        .filter { !it.isClosed }
        .toList()
        .maxByOrNull { it.lastStatusChange }

    suspend fun registerConnection(profileId: Long, nodeAddress: UUID) = connectionRepository.save(
        Connection(
            profileId,
            nodeAddress,
            isClosed = false,
        )
    )

    suspend fun closeConnection(profileId: Long, nodeAddress: UUID) = connectionRepository.save(
        Connection(
            profileId,
            nodeAddress,
            isClosed = true,
        )
    )
}