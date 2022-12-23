package ru.zveron

import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import javax.sql.DataSource

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
abstract class BlacklistTest {
    @Autowired
    lateinit var dataSource: DataSource

    companion object {
        private val container = PostgreSQLContainer("postgres:13.1-alpine").apply {
            withDatabaseName("test_db")
            withUsername("test_db_name")
            withPassword("test_db_password")
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", container::getJdbcUrl)
            registry.add("spring.datasource.password", container::getPassword)
            registry.add("spring.datasource.username", container::getUsername)
        }

        init {
            container.start()
        }
    }

    @BeforeEach
    fun `Cleaning database`() {
        val connection = dataSource.connection
        val statement = connection.createStatement()
        statement.execute(
            "TRUNCATE blacklist_record"
        )

        statement.close()
        connection.close()
    }
}