package ru.zveron.config

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.CqlSessionBuilder
import io.kotest.assertions.fail
import mu.KLogging
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.io.FileSystemResource
import org.springframework.data.cassandra.core.cql.session.init.ScriptUtils
import java.io.File

@TestConfiguration
class ScyllaInitConfig {
    companion object : KLogging()

    @Bean
    fun cqlSession(cqlSessionBuilder: CqlSessionBuilder): CqlSession {
        val changelog = File("src/main/resources/db/changelog")
        val scripts = changelog.listFiles() ?: fail("No scripts in changelog are present")
        cqlSessionBuilder.build().use { session ->
            for (file in scripts) {
                logger.info("Executing init script: ${file.path}")
                ScriptUtils.executeCqlScript(session, FileSystemResource(file.path))
            }
        }
        return cqlSessionBuilder.withKeyspace("zveron_chat").build()
    }
}