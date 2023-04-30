package ru.zveron.component

import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component
import ru.zveron.model.entity.Connection
import ru.zveron.repository.ConnectionRepository
import java.time.Instant
import java.util.UUID

@Component
class ConnectionStorage(val connectionRepository: ConnectionRepository) {

    suspend fun getConnectionWithNewestStatusChange(profileId: Long) = connectionRepository
        .findAllOpenConnectionsByProfile(profileId)
        .toList()
        .maxByOrNull { it.lastStatusChange }

    suspend fun registerConnection(profileId: Long, nodeAddress: UUID) = connectionRepository.save(
        Connection(
            profileId,
            nodeAddress,
            true,
            Instant.now(),
        )
    )

    suspend fun closeConnection(profileId: Long, nodeAddress: UUID) = connectionRepository.save(
        Connection(
            profileId,
            nodeAddress,
            false,
            Instant.now(),
        )
    )
}