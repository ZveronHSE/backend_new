package ru.zveron.util

object StringUtil {
    fun String.toList() = this.split(",").map { it.trim() }
}
