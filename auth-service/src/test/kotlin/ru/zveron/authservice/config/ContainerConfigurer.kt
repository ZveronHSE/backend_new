package ru.zveron.authservice.config

import io.opentelemetry.api.GlobalOpenTelemetry
import mu.KLogging
import org.junit.jupiter.api.BeforeAll
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer


@AutoConfigureWireMock(port = 0) //random port
abstract class ContainerConfigurer {

    companion object : KLogging() {
        private val postgreSQLContainer = PostgreSQLContainer<Nothing>("postgres:13")

        @DynamicPropertySource
        @JvmStatic
        fun properties(registry: DynamicPropertyRegistry) {
            val r2dbcUrl = "r2dbc:postgresql://${postgreSQLContainer.host}:" +
                    "${postgreSQLContainer.firstMappedPort}/${postgreSQLContainer.databaseName}"

            logger.info { "Postgres URL: $r2dbcUrl" }

            registry.add("spring.r2dbc.url") { r2dbcUrl }
            registry.add("spring.r2dbc.username", postgreSQLContainer::getUsername)
            registry.add("spring.r2dbc.password", postgreSQLContainer::getPassword)

            registry.add("spring.liquibase.url", postgreSQLContainer::getJdbcUrl)
            registry.add("spring.liquibase.user", postgreSQLContainer::getUsername)
            registry.add("spring.liquibase.password", postgreSQLContainer::getPassword)
        }

        init {
            postgreSQLContainer.start()
        }

        @JvmStatic
        @BeforeAll
        fun setUp() {
            GlobalOpenTelemetry.resetForTest()
        }
    }

}
