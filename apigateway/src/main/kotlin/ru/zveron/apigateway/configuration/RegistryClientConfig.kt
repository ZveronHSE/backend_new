package ru.zveron.apigateway.configuration

import io.netty.handler.logging.LogLevel
import io.netty.handler.timeout.ReadTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import reactor.netty.transport.logging.AdvancedByteBufFormat
import java.util.concurrent.TimeUnit

@Configuration
class RegistryClientConfig {


    @Bean
    fun registryWebClient(builder: WebClient.Builder): WebClient {
        return builder.clientConnector(
            ReactorClientHttpConnector(
                HttpClient.create(
                    ConnectionProvider.builder("registry-client-pool")
                        .maxConnections(ConnectionProvider.DEFAULT_POOL_MAX_CONNECTIONS)
                        .build()
                ).debugEnabled()
                    .doOnConnected { connection ->
                        connection.addHandlerLast(ReadTimeoutHandler(5000L, TimeUnit.MILLISECONDS))
                    }
            )
        )
            .baseUrl("localhost:8761")
            .build()
    }
}

fun HttpClient.debugEnabled(): HttpClient =
    wiretap(
        "reactor.netty.http.client.HttpClient",
        LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL
    )