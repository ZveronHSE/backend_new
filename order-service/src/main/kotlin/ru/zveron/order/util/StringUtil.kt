package ru.zveron.order.util

object StringUtil {
    fun String.toList() = this.split(",").map { it.trim() }
}