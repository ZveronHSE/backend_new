package ru.zveron

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient

@EnableEurekaClient
@SpringBootApplication
class LotServiceApplication

fun main(args: Array<String>) {
    runApplication<LotServiceApplication>(*args)
}