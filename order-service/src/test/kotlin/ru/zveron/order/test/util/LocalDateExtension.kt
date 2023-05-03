package ru.zveron.order.test.util

import io.kotest.matchers.shouldBe
import ru.zveron.order.test.util.LocalDateUtil.isAfterOrEqual
import ru.zveron.order.test.util.LocalDateUtil.isBeforeOrEqual
import java.time.LocalDate


infix fun LocalDate.shouldBeAfterOrEqual(date: LocalDate) = this.isAfterOrEqual(date) shouldBe true
infix fun LocalDate.shouldBeBeforeOrEqual(date: LocalDate) = this.isBeforeOrEqual(date) shouldBe true
