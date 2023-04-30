package ru.zveron.order.util

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class PriceFormatterTest {

    @Test
    fun `given price, then returns formatted price`() {
        val price = "1000"

        val formattedPrice = PriceFormatter.formatToPrice(price)

        formattedPrice shouldBe "1000 ₽"
    }

    @Test
    fun `given price 0, then returns formatted price`() {
        val price = "0"

        val formattedPrice = PriceFormatter.formatToPrice(price)

        formattedPrice shouldBe "Договорная"
    }
}