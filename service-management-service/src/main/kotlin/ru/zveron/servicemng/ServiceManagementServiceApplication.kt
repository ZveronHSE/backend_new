package ru.zveron.servicemng

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ServiceManagementServiceApplication

fun main(args: Array<String>) {
    runApplication<ServiceManagementServiceApplication>(*args)
}
