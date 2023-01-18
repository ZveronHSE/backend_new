package ru.zveron.apigateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient

@EnableEurekaClient
@SpringBootApplication
class ApigatewayApplication

fun main(args: Array<String>) {
    runApplication<ApigatewayApplication>(*args)
}
