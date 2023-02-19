package ru.zveron.grpc

import com.google.protobuf.Empty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import ru.zveron.DataBaseApplicationTest
import ru.zveron.service.CategoryService
import ru.zveron.util.CreateEntitiesUtils
import ru.zveron.util.CreateEntitiesUtils.mapCategoriesToExternalResponse

@DirtiesContext
class CategoryExternalControllerTest : DataBaseApplicationTest() {
    @Autowired
    lateinit var categoryExternalController: CategoryExternalController

    @Autowired
    lateinit var categoryService: CategoryService

    companion object {
        const val ROOT_CATEGORY_ID = 1
    }

    @Test
    fun `GetChildren correct request for response`(): Unit = runBlocking {
        val expected = mapCategoriesToExternalResponse(*categoryService.getChildren(ROOT_CATEGORY_ID).toTypedArray())

        val actualResponse =
            categoryExternalController.getChildren(CreateEntitiesUtils.mockIntWrapper(ROOT_CATEGORY_ID))

        actualResponse shouldBe expected
    }

    @Test
    fun `GetRoot correct request for response`(): Unit = runBlocking {
        val categories = categoryExternalController.getRoot(Empty.getDefaultInstance())

        categories.categoriesCount shouldBe 1
        categories.categoriesList[0].id shouldBe ROOT_CATEGORY_ID
    }
}