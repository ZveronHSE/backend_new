package ru.zveron

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BlacklistServiceApplication

fun main(args: Array<String>) {
    runApplication<BlacklistServiceApplication>(*args)
}