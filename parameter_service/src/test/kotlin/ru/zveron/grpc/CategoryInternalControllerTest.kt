package ru.zveron.grpc

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.mockk.every
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import ru.zveron.DataBaseApplicationTest
import ru.zveron.entity.Category
import ru.zveron.service.CategoryService
import ru.zveron.util.CreateEntitiesUtils.mapToInternalCategory
import ru.zveron.util.CreateEntitiesUtils.mockIntWrapper
import ru.zveron.util.GeneratorUtils.generateInt

@DirtiesContext
class CategoryInternalControllerTest : DataBaseApplicationTest() {
    @Autowired
    lateinit var categoryInternalController: CategoryInternalController

    @MockkBean
    lateinit var categoryService: CategoryService

    @Test
    fun `CategoryHasChildren correct request for category which has children`(): Unit = runBlocking {
        val categoryId = generateInt()

        every {
            categoryService.getChildren(categoryId)
        } returns listOf(Category(categoryId + 1, "name"))

        val response = categoryInternalController.categoryHasChildren(mockIntWrapper(categoryId))

        response.value.shouldBeTrue()
    }

    @Test
    fun `CategoryHasChildren correct request for category which hasn't children`(): Unit = runBlocking {
        val categoryId = generateInt()

        every {
            categoryService.getChildren(categoryId)
        } returns listOf()

        val response = categoryInternalController.categoryHasChildren(mockIntWrapper(categoryId))

        response.value.shouldBeFalse()
    }

    @Test
    fun `GetCategoryTree correct request for category with tree`(): Unit = runBlocking {
        val categoryId = generateInt()
        val categories = listOf(Category(categoryId + 1, "name"), Category(categoryId + 2, "name2"))
        every {
            categoryService.getTree(categoryId)
        } returns categories

        val actual = categoryInternalController.getCategoryTree(mockIntWrapper(categoryId))
        val expected = categories.map { mapToInternalCategory(it) }

        actual.categoriesList shouldContainExactlyInAnyOrder expected
    }
}