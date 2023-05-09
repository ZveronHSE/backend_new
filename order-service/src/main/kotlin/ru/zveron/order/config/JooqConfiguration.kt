package ru.zveron.order.config

import io.r2dbc.spi.ConnectionFactory
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JooqConfiguration(
    private val connectionFactory: ConnectionFactory
) {

    @Bean
    fun jooqContext(): DSLContext {
        val settings = Settings().withExecuteLogging(true)

        return DSL
                .using(connectionFactory, SQLDialect.POSTGRES, settings)
                .dsl()
    }
}
