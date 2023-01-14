package ru.zveron.apigateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.netflix.eureka.EnableEurekaClient

@EnableDiscoveryClient
@EnableEurekaClient
@SpringBootApplication
class ApigatewayApplication

fun main(args: Array<String>) {
    runApplication<ApigatewayApplication>(*args)
}
