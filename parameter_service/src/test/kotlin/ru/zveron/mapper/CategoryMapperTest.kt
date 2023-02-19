package ru.zveron.mapper

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import ru.zveron.mapper.CategoryMapper.toCategoryResponse
import ru.zveron.util.GeneratorUtils.generateCategories

class CategoryMapperTest {
    @Test
    fun `Mapping from list categories entity to external category response`() {
        val categories = generateCategories()

        val actualCategories = categories.toCategoryResponse()

        actualCategories.categoriesCount shouldBe categories.size

        actualCategories.categoriesList.forEachIndexed { index, category ->
            category.id shouldBe categories[index].id
            category.name shouldBe categories[index].name
        }
    }
}