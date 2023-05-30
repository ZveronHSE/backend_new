package ru.zveron.review.util

object StringUtil {
    fun String.toList() = this.split(",").map { it.trim() }
}
