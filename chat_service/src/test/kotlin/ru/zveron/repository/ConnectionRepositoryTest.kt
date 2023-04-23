package ru.zveron.repository

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.ChatServiceApplicationTest
import ru.zveron.common.assertion.ConnectionAssertions.connectionShouldBe
import ru.zveron.common.generator.PrimitivesGenerator.generateLongs
import ru.zveron.common.generator.PrimitivesGenerator.generateNTimeUuids
import ru.zveron.model.entity.Connection
import java.time.Instant

class ConnectionRepositoryTest : ChatServiceApplicationTest() {

    @Autowired
    lateinit var connectionRepository: ConnectionRepository

    @Test
    fun `findAllOpenConnectionsByProfile when multiple online connections`() {
        val timestamp = Instant.now()
        val (user1, user2) = generateLongs(2)
        val (node1, node2) = generateNTimeUuids(2)

        val connection1 = Connection(user1, node1, true, timestamp)
        val connection2 = Connection(user1, node2, false, timestamp.plusSeconds(1))
        val connection3 = Connection(user1, node1, false, timestamp.plusSeconds(3))
        val connection4 = Connection(user2, node2, true, timestamp)
        val connection5 = Connection(user2, node1, false, timestamp.plusSeconds(1))
        val connection6 = Connection(user2, node2, false, timestamp.plusSeconds(3))

        runBlocking {
            connectionRepository.save(connection1)
            connectionRepository.save(connection2)
            connectionRepository.save(connection3)
            connectionRepository.save(connection4)
            connectionRepository.save(connection5)
            connectionRepository.save(connection6)

            connectionRepository.findAllOpenConnectionsByProfile(user1).toList().apply {
                first() connectionShouldBe connection3
                component2() connectionShouldBe connection2
            }
        }
    }
}