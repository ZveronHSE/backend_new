package ru.zveron

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ParameterServiceApplication

fun main(args: Array<String>) {
	runApplication<ParameterServiceApplication>(*args)
}
