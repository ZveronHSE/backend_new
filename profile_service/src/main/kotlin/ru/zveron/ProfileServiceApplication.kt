package ru.zveron

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient

@SpringBootApplication
@EnableEurekaClient
class ProfileServiceApplication

fun main(args: Array<String>) {
    runApplication<ProfileServiceApplication>(*args)
}