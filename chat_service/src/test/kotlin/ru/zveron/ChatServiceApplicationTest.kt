package ru.zveron

import com.datastax.oss.driver.api.core.CqlSession
import com.ninjasquad.springmockk.MockkBean
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import ru.zveron.client.blacklist.BlacklistClient
import ru.zveron.client.lot.LotClient
import ru.zveron.client.profile.ProfileClient

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = ["eureka.client.enabled=false", "spring.data.cassandra.local-datacenter=datacenter1"])
abstract class ChatServiceApplicationTest {

    @Autowired
    lateinit var session: CqlSession

    companion object {
        private val scyllaDb = GenericContainer(DockerImageName.parse("scylladb/scylla:5.1"))
            .withExposedPorts(9042)

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            scyllaDb.start()

            registry.add("spring.data.cassandra.contact-points") { scyllaDb.host + ":" + scyllaDb.firstMappedPort }
        }
    }

    @BeforeEach
    fun `Cleaning database`() {
        session.execute("TRUNCATE zveron_chat.chat;")
        session.execute("TRUNCATE zveron_chat.message;")
        session.execute("TRUNCATE zveron_chat.connection;")
    }


    @MockkBean
    lateinit var profileClient: ProfileClient

    @MockkBean
    lateinit var lotClient: LotClient

    @MockkBean
    lateinit var blacklistClient: BlacklistClient
}