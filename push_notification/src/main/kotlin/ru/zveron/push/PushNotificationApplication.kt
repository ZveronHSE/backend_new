package ru.zveron.push

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PushNotificationApplication

fun main(args: Array<String>) {
    runApplication<PushNotificationApplication>(*args)
}
