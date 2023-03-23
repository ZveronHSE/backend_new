package ru.zveron.objectstorage.util

import org.apache.commons.lang3.RandomUtils
import org.springframework.http.MediaType
import java.util.UUID


inline fun <reified T : Enum<T>> randomEnum() = enumValues<T>().random()

fun randomImageBytes() = RandomUtils.nextBytes(10)

fun randomKey() = """key-${UUID.randomUUID()}"""

fun randomBucket() = """bucket-${UUID.randomUUID()}"""

fun randomImageMediaType() = listOf(MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE).shuffled().first()