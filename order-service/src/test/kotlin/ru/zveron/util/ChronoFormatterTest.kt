package ru.zveron.util

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class ChronoFormatterTest {

    @Test
    fun `given service date from and to, then returns service date in correct format`() {
        val dateFrom = LocalDate.of(2021, 1, 1)
        val dateTo = LocalDate.of(2021, 1, 2)

        val formattedDate = ChronoFormatter.formatServiceDate(dateFrom, dateTo)

        formattedDate shouldBe "01.01.2021 - 02.01.2021"
    }

    @Test
    fun `given service date from, then returns service date in correct format`() {
        val dateFrom = LocalDate.of(2021, 1, 1)

        val formattedDate = ChronoFormatter.formatServiceDate(dateFrom, null)

        formattedDate shouldBe "01.01.2021"
    }

    @Test
    fun `given service time from and to, then returns service time in correct format`() {
        val timeFrom = LocalTime.of(10, 0)
        val timeTo = LocalTime.of(11, 0)

        val formattedTime = ChronoFormatter.formatServiceTime(timeFrom, timeTo)

        formattedTime shouldBe "10:00 - 11:00"
    }

    @Test
    fun `given service time from, then returns service time in correct format`() {
        val timeFrom = LocalTime.of(10, 0)

        val formattedTime = ChronoFormatter.formatServiceTime(timeFrom, null)

        formattedTime shouldBe "10:00"
    }

    @Test
    fun `given no service time bounds, then returns empty string`() {
        val formattedTime = ChronoFormatter.formatServiceTime(null, null)

        formattedTime shouldBe ""
    }

    @Test
    fun `given created at is today, then returns formatted time`() {
        val createdAt = Instant.from(
            ZonedDateTime.of(
                LocalDate.now(),
                LocalTime.of(10, 0),
                ZoneId.of("Europe/Moscow")
            )
        )

        val formattedCreatedAt = ChronoFormatter.formatCreatedAt(createdAt)

        formattedCreatedAt shouldBe "Сегодня в 10:00"
    }

    @Test
    fun `given created at is not today, then returns formatted date`() {
        val createdAt = Instant.from(
            ZonedDateTime.of(
                LocalDate.of(2021, 1, 1),
                LocalTime.of(10, 0),
                ZoneId.of("Europe/Moscow")
            )
        )

        val formattedCreatedAt = ChronoFormatter.formatCreatedAt(createdAt)

        formattedCreatedAt shouldBe "01.01.2021"
    }
}