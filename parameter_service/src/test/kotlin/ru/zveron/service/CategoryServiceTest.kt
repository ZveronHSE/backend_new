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
    fun `GetChildOfRootAncestor Success get child of root category`() {
        val childChildCategory = categoryRepository.save(mockCategoryWithParent(childCategory))

        val response = categoryService.getChildOfRootAncestor(childChildCategory.id)

        response shouldBe childCategory
    }

    @Test
    fun `GetChildOfRootAncestor Should throw exception, if get category root`() {
        shouldThrow<CategoryException> { categoryService.getChildOfRootAncestor(rootCategory.id) }
    }

    @Test
    fun `GetChildOfRootAncestor Should return same entity, if get category which already child of root category`() {
        val response = categoryService.getChildOfRootAncestor(childCategory.id)

        response shouldBe childCategory
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
}