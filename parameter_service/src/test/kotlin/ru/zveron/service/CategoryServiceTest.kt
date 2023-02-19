package ru.zveron.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.DataBaseApplicationTest
import ru.zveron.entity.Category
import ru.zveron.exception.CategoryException
import ru.zveron.repository.CategoryRepository
import ru.zveron.util.CreateEntitiesUtils.mockCategoryWithParent
import ru.zveron.util.CreateEntitiesUtils.mockRootCategory

internal class CategoryServiceTest : DataBaseApplicationTest() {

    @Autowired
    lateinit var categoryService: CategoryService

    @Autowired
    lateinit var categoryRepository: CategoryRepository

    lateinit var rootCategory: Category
    lateinit var childCategory: Category

    companion object {
        const val UNKNOWN_ID = 100500
    }

    @BeforeEach
    fun `Create default categories`() {
        rootCategory = categoryRepository.save(mockRootCategory())
        childCategory = categoryRepository.save(mockCategoryWithParent(rootCategory))
    }

    @Test
    fun `GetChildren Get only children of category`() {
        val childCategory1 = categoryRepository.save(mockCategoryWithParent(rootCategory))
        categoryRepository.save(mockCategoryWithParent(childCategory))

        val response = categoryService.getChildren(rootCategory.id)

        response shouldBe listOf(childCategory, childCategory1)
    }

    @Test
    fun `GetChildren Dont get children of category, if dont have`() {
        val response = categoryService.getChildren(childCategory.id)

        response.shouldBeEmpty()
    }

    @Test
    fun `GetTree Get full family of category`() {
        val response = categoryService.getTree(rootCategory.id)

        response shouldBe listOf(rootCategory, childCategory)
    }

    @Test
    fun `GetTree Should throw exception if category id not found`() {
        shouldThrow<CategoryException> { categoryService.getTree(UNKNOWN_ID) }
    }

    @Test
    fun `GetRootCategoryByChild Get correct root category by child category id`() {
        val result = categoryService.getRootCategoryByChild(childCategory)

        result shouldBe rootCategory
    }

    @Test
    fun `GetRootCategoryByChild Get correct root category by child child category id`() {
        val childChildCategory = categoryRepository.save(mockCategoryWithParent(childCategory))
        val result = categoryService.getRootCategoryByChild(childChildCategory)

        result shouldBe rootCategory
    }

    @Test
    fun `GetRootCategoryByChild return root category if get it`() {
        val result = categoryService.getRootCategoryByChild(rootCategory)

        result shouldBe rootCategory
    }
}

