package ru.zveron.apigateway.config

import mu.KLogging
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer

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
    }
}
