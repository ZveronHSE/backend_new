package ru.zv.authservice.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JsonTestConfig {
    @Bean
    fun jsonMapper(): ObjectMapper = JsonMapper().findAndRegisterModules()
}