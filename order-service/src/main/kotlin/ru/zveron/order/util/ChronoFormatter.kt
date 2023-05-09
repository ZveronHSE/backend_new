package ru.zveron.order.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object ChronoFormatter {
    private val dayMonthYearFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    private val hourMinuteFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private val MOSCOW_ZONE_ID = ZoneId.of("Europe/Moscow")


    /**
     * formats service date to dd.MM.yyyy - dd.MM.yyyy format if serviceDateTo is not null
     * dd.MM.yyyy otherwise
     *
     * @param serviceDateFrom
     * @param serviceDateTo
     */
    fun formatServiceDate(serviceDateFrom: LocalDate, serviceDateTo: LocalDate?) =
        """${toDdMmYyyy(serviceDateFrom)}${serviceDateTo?.let { " - ${toDdMmYyyy(it)}" } ?: ""}"""


    /**
     * If either parts of the time window are present, then returns formatted time window as "HH:mm - HH:mm"
     * Else if only the starting part is present, then returns formatted time window as "HH:mm"
     * Otherwise returns empty string
     *
     * @param timeWindowFrom
     * @param timeWindowTo
     */
    fun formatServiceTime(timeWindowFrom: LocalTime?, timeWindowTo: LocalTime?) =
        timeWindowFrom?.let { from -> "$from${timeWindowTo?.let { to -> " - $to" } ?: ""}" }
            ?: ""


    /**
     * If createdAt is today, then returns formatted time as "Сегодня в HH:mm"
     * Else returns formatted date as "dd.MM.yyyy"
     *
     * @param createdAt
     */
    fun formatCreatedAt(createdAt: Instant) =
        if (Instant.now().truncatedTo(ChronoUnit.DAYS) == createdAt.truncatedTo(ChronoUnit.DAYS)) {
            "Сегодня в ${
                createdAt
                    .let { ZonedDateTime.ofInstant(it, MOSCOW_ZONE_ID) }
                    .let { toHhMm(it) }
            }"
        } else {
            createdAt
                .let { ZonedDateTime.ofInstant(it, MOSCOW_ZONE_ID) }
                .let { toDdMmYyyy(it) }
        }

    private fun toDdMmYyyy(date: LocalDate): String = dayMonthYearFormatter.format(date)

    private fun toDdMmYyyy(date: ZonedDateTime): String = date.let { dayMonthYearFormatter.format(it) }

    private fun toHhMm(date: ZonedDateTime): String = date.let { hourMinuteFormatter.format(it) }
}
