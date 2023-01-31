package ru.zveron

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableEurekaClient
@EnableJpaRepositories(basePackages = ["ru.zveron"])
class BlacklistServiceApplication

fun main(args: Array<String>) {
    runApplication<BlacklistServiceApplication>(*args)
}