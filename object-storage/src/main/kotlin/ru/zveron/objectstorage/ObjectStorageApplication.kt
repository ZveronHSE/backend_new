package ru.zveron.objectstorage

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ObjectStorageApplication

fun main(args: Array<String>) {
    runApplication<ObjectStorageApplication>(*args)
}
