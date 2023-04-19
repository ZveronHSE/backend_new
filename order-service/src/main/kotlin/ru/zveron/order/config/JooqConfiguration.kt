package ru.zveron.order.config

import io.r2dbc.spi.ConnectionFactory
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JooqConfiguration(
        private val connectionFactory: ConnectionFactory
) {

    @Bean
    fun jooqContext(): DSLContext {
        return DSL
                .using(connectionFactory)
                .dsl()

    }
}