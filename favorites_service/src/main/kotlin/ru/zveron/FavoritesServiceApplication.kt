package ru.zveron

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FavoritesServiceApplication

fun main(args: Array<String>) {
    runApplication<FavoritesServiceApplication>(*args)
}