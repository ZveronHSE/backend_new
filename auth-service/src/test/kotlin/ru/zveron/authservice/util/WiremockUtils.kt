package ru.zveron.authservice.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

private val om = ObjectMapper().findAndRegisterModules().registerKotlinModule()

fun ResponseDefinitionBuilder.withJsonDto(dto: Any) = this.withBody(om.writeValueAsString(dto))
    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)

fun WireMockServer.getHost() = "http://localhost:${this.port()}"