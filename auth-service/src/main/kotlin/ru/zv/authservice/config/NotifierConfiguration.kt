package ru.zv.authservice.config

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.validation.annotation.Validated
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import ru.zv.authservice.webclient.NotifierClient
import java.util.concurrent.TimeUnit

@Configuration
@EnableConfigurationProperties(NotifierProperties::class)
class NotifierConfiguration {

    @Bean
    fun notifierClient(
        builder: WebClient.Builder,
        properties: NotifierProperties,
    ): NotifierClient {
        val webclient = builder.baseUrl(properties.baseUrl)
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient.create(
                        ConnectionProvider.builder("notifier-client-pool")
                            .maxConnections(properties.maxConnections)
                            .build()
                    ).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.connectionTimeoutMs)
                        .doOnConnected { connection ->
                            connection.addHandlerLast(
                                ReadTimeoutHandler(
                                    properties.readTimeoutMs,
                                    TimeUnit.MILLISECONDS
                                )
                            )
                        }
                        .compress(true)
                )
            )
            .build()

        return NotifierClient(webclient)
    }
}

@Validated
@ConstructorBinding
@ConfigurationProperties(value = "clients.notifier")
data class NotifierProperties(
    val baseUrl: String,
    val maxConnections: Int = 10,
    val connectionTimeoutMs: Int = 5000,
    val readTimeoutMs: Long = 5000L,
)