package ru.zveron.order.util

import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object ChronoFormatter {
    private val dayMonthYearFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    private val hourMinuteFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun toDdMmYyyy(date: LocalDate): String = dayMonthYearFormatter.format(date)

    fun toDdMmYyyy(date: ZonedDateTime): String = date.let { dayMonthYearFormatter.format(it) }

    fun toHhMm(date: ZonedDateTime): String = date.let { hourMinuteFormatter.format(it) }
}