package ru.zveron

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AddressServiceApplication

fun main(args: Array<String>) {
	// Имитирую изменения в другом модуле
	runApplication<AddressServiceApplication>(*args)
}