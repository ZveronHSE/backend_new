package ru.zveron.service

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.DataBaseApplicationTest

internal class LotFormServiceTest : DataBaseApplicationTest() {
    @Autowired
    lateinit var lotFormService: LotFormService


    companion object {
        const val CATEGORY_ID = 1
    }

    @Test
    fun `Success get lotforms by category id`() {
        val response = lotFormService.getLotFormsByCategoryId(CATEGORY_ID)

        response.forEach {
            it.category.id shouldBe CATEGORY_ID
        }
    }

    @Test
    fun `If didn't get lot form by category id, because it doesnt exists - should return empty list`() {
        val mockId = 100500

        val lotForms = lotFormService.getLotFormsByCategoryId(mockId)

        lotForms.shouldBeEmpty()
    }
}