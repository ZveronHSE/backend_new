package ru.zveron.authservice.webclient

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.configureFor
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.web.reactive.function.client.WebClient

abstract class BaseWiremockTest {
    companion object {

        val server = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort())
        lateinit var webClient: WebClient

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            val om =
                ObjectMapper().registerKotlinModule().registerModule(ParameterNamesModule())
            server.start()
            configureFor(server.port())
            webClient = WebClient.builder().baseUrl(server.baseUrl())
                .codecs { it.customCodecs().registerWithDefaultConfig(Jackson2JsonDecoder(om)) }
                .build()
        }

        @AfterAll
        @JvmStatic
        fun afterAll() {
            server.stop()
        }
    }

    @BeforeEach
    fun beforeEach() {
        server.resetAll()
    }
}