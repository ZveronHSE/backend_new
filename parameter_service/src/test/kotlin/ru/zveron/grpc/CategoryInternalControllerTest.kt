package ru.zveron.grpc

import com.ninjasquad.springmockk.MockkBean
import io.kotest.assertions.asClue
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
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
    fun `CategoryHasChildren correct request for category which has children and animal`(): Unit = runBlocking {
        val categoryId = generateInt()

        every {
            categoryService.getChildren(categoryId)
        } returns listOf(Category(categoryId + 1, "name"))

        every {
            categoryService.getCategoryByIDOrThrow(categoryId)
        } returns Category(categoryId, "main")

        every {
            categoryService.getRootCategoryByChild(Category(categoryId, "main"))
        } returns Category(1, "animal")

        val response = categoryInternalController.getInfoAboutCategory(mockIntWrapper(categoryId))

        response.asClue {
            it.category.asClue {
                it.id shouldBe categoryId
                it.name shouldBe "main"
            }

            it.hasChildren.shouldBeTrue()
            it.hasGender.shouldBeTrue()
        }
    }

    @Test
    fun `CategoryHasChildren correct request for category which hasn't children and not animal`(): Unit = runBlocking {
        val categoryId = generateInt()

        every {
            categoryService.getChildren(categoryId)
        } returns listOf()

        every {
            categoryService.getCategoryByIDOrThrow(categoryId)
        } returns Category(categoryId, "main")

        every {
            categoryService.getRootCategoryByChild(Category(categoryId, "main"))
        } returns Category(2, "goods")

        val response = categoryInternalController.getInfoAboutCategory(mockIntWrapper(categoryId))

        response.hasChildren.shouldBeFalse()
        response.hasGender.shouldBeFalse()
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