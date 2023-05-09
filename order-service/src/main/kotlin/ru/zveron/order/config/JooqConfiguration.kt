package ru.zveron.order.config

import io.r2dbc.spi.ConnectionFactory
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.zveron.order.persistence.model.constant.Status

@Configuration
class JooqConfiguration(
    private val connectionFactory: ConnectionFactory
) {

    @Bean
    fun jooqContext(): DSLContext {
        val settings = Settings().withExecuteLogging(true)

        val dsl = DSL
            .using(connectionFactory, SQLDialect.POSTGRES, settings)
            .dsl()

        dsl.createType("status")
            .asEnum(Status.values().map { it.name })
            .executeAsync()

        return dsl
    }
}