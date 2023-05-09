package ru.zveron.order.test.util

import java.time.LocalDate

object LocalDateUtil {
    fun LocalDate.isAfterOrEqual(date: LocalDate): Boolean {
        return this.isAfter(date) || this.isEqual(date)
    }

    fun LocalDate.isBeforeOrEqual(date: LocalDate): Boolean {
        return this.isBefore(date) || this.isEqual(date)
    }
}
