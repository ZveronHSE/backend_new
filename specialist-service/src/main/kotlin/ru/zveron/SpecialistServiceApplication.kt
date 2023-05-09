package ru.zveron

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient

@EnableEurekaClient
@SpringBootApplication
class SpecialistServiceApplication

fun main(args: Array<String>) {
    runApplication<SpecialistServiceApplication>(*args)
}
