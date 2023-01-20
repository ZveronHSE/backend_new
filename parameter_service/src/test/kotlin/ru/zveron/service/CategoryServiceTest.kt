package ru.zveron.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.DataBaseApplicationTest
import ru.zveron.contract.category.categoryResponse
import ru.zveron.entity.Category
import ru.zveron.exception.CategoryException
import ru.zveron.repository.CategoryRepository
import ru.zveron.util.CreateEntitiesUtils.mapCategoriesToResponse
import ru.zveron.util.CreateEntitiesUtils.mockCategoryRequest
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
    fun `GetChild Get only children of category`(): Unit = runBlocking {
        val childCategory1 = categoryRepository.save(mockCategoryWithParent(rootCategory))
        categoryRepository.save(mockCategoryWithParent(childCategory))

        val response = categoryService.getChild(
            mockCategoryRequest(rootCategory.id)
        )

        response shouldBe mapCategoriesToResponse(childCategory, childCategory1)
    }

    @Test
    fun `GetChild Should throw exception if category id not found`(): Unit = runBlocking {
        shouldThrow<CategoryException> {
            categoryService.getChild(
                mockCategoryRequest(UNKNOWN_ID)
            )
        }
    }

    @Test
    fun `GetChild Dont get children of category, if dont have`(): Unit = runBlocking {
        val response = categoryService.getChild(
            mockCategoryRequest(childCategory.id)
        )

        response shouldBe categoryResponse {

        }
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
    fun `GetCategoryTree Get full family of category`(): Unit =
        runBlocking {
            val response = categoryService.getCategoryTree(mockCategoryRequest(rootCategory.id))

            response shouldBe mapCategoriesToResponse(rootCategory, childCategory)
        }

    @Test
    fun `GetCategoryTree Should throw exception if category id not found`(): Unit =
        runBlocking {
            shouldThrow<CategoryException> { categoryService.getCategoryTree(mockCategoryRequest(UNKNOWN_ID)) }
        }
}