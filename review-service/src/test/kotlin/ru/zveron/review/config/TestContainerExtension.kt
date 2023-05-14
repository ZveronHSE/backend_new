package ru.zveron.review.config

import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ConditionEvaluationResult
import org.junit.jupiter.api.extension.ExecutionCondition
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.PostgreSQLContainer

object TestContainerExtension : BeforeAllCallback, ExecutionCondition {

    private var pgError: Throwable? = null

    private val PG = PostgreSQLContainer<Nothing>("postgres:13").apply {
        try {
            println("Starting Postgresql Container")
            start()
        } catch (ex: Exception) {
            pgError = ex
        }
    }

    override fun beforeAll(context: ExtensionContext?) {
        if (pgError != null) {
            throw RuntimeException("${pgError?.message}")
        }
    }

    override fun evaluateExecutionCondition(context: ExtensionContext): ConditionEvaluationResult {
        if (pgError != null && context.testMethod.isPresent) {
            if (pgError is NullPointerException) {
                return ConditionEvaluationResult.disabled("$pgError")
            }
            return ConditionEvaluationResult.disabled("${pgError?.message}")
        }
        return ConditionEvaluationResult.enabled("")
    }

    fun registerStaticProperties(registry: DynamicPropertyRegistry) {
        val r2dbcUrl = "r2dbc:postgresql://${PG.host}:${PG.firstMappedPort}/${PG.databaseName}"
        registry.add("spring.r2dbc.url") { r2dbcUrl }
        registry.add("spring.r2dbc.username", PG::getUsername)
        registry.add("spring.r2dbc.password", PG::getPassword)
        registry.add("spring.liquibase.url", PG::getJdbcUrl)
        registry.add("spring.liquibase.user", PG::getUsername)
        registry.add("spring.liquibase.password", PG::getPassword)
    }
}
