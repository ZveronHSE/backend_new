package ru.zveron.config

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.CqlSessionBuilder
import com.datastax.oss.driver.api.core.uuid.Uuids
import mu.KLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.data.cassandra.core.cql.session.init.ScriptUtils
import java.lang.RuntimeException
import java.time.Instant

@Configuration
class ScyllaDbConfig {
    companion object : KLogging()

    val keyspaceName: String = "zveron_chat"

    // TODO в ликвибейзе есть лок на бд, я так понимаю для кейса когда несколько инстансов приложения

    @Bean
    fun getCqlSession(cqlSessionBuilder: CqlSessionBuilder): CqlSession {
        cqlSessionBuilder.build().use { session ->
            prepareDb(session)
            executeChangeset(session)
        }
        return cqlSessionBuilder.withKeyspace(keyspaceName).build()
    }

    private fun prepareDb(session: CqlSession) {
        logger.info("Preparing DB...")
        val scripts = PathMatchingResourcePatternResolver().getResources("classpath:db/init/*cql").toList()
        if (scripts.isEmpty()) {
            throw RuntimeException("No init scripts are present")
        }
        scripts.sortedBy { it.filename }.forEach {
            logger.info("Executing init script: ${it.filename}")
            ScriptUtils.executeCqlScript(session, it)
        }
    }

    private fun executeChangeset(session: CqlSession) {
        // TODO выполнять по запросу а не по целому файлу
        val checkChangeSet =
            session.prepare("select count(*) from $keyspaceName.changelog where file_name = ? allow filtering")
        val insertChangesetInfo =
            session.prepare("insert into $keyspaceName.changelog (id, file_name, date_executed) values (?, ?, ?)")

        logger.info("Running changelogs...")
        val scripts = PathMatchingResourcePatternResolver().getResources("classpath:db/changelog/*cql").toList()
        scripts.sortedBy { it.filename }.forEach {
            if (session.execute(checkChangeSet.bind(it.filename)).one()?.getLong(0) == 0L) {
                logger.info("Executing changelog script: ${it.filename}")
                ScriptUtils.executeCqlScript(session, it)
                session.execute(insertChangesetInfo.bind(Uuids.timeBased(), it.filename, Instant.now()))
            } else {
                logger.info("Changelog script already applied: ${it.filename}")
            }
        }
    }
}