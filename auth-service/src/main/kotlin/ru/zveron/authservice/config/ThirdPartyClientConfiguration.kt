package ru.zveron.authservice.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import ru.zveron.authservice.webclient.thirdparty.ThirdPartyClient
import java.util.concurrent.TimeUnit

@Configuration
@EnableConfigurationProperties(ThirdPartyClientProperties::class)
class ThirdPartyClientConfiguration {

    @Bean
    fun thirdPartyClient(
        builder: WebClient.Builder,
        clientProperties: NotifierProperties,
        jsonMapper: ObjectMapper,
    ): ThirdPartyClient {
        val webclient = builder.clientConnector(
            ReactorClientHttpConnector(
                HttpClient.create(
                    ConnectionProvider.builder("third-party-client-pool")
                        .maxConnections(clientProperties.maxConnections)
                        .build()
                ).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientProperties.connectionTimeoutMs)
                    .doOnConnected { connection ->
                        connection.addHandlerLast(
                            ReadTimeoutHandler(
                                clientProperties.readTimeoutMs,
                                TimeUnit.MILLISECONDS
                            )
                        )
                    }
                    .compress(true)
            )
        )
            .build()

        return ThirdPartyClient(webclient, jsonMapper)
    }
}
