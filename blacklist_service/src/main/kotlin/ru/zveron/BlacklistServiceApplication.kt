package ru.zveron

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient

@SpringBootApplication
@EnableEurekaClient
class BlacklistServiceApplication

fun main(args: Array<String>) {
    runApplication<BlacklistServiceApplication>(*args)
}