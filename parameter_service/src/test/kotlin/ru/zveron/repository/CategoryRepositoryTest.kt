package ru.zveron.repository

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.DataBaseApplicationTest
import ru.zveron.util.CreateEntitiesUtils

class CategoryRepositoryTest : DataBaseApplicationTest() {

    @Autowired
    lateinit var categoryRepository: CategoryRepository

    @Test
    fun `Correct get family by category`() {
        val rootCategory = categoryRepository.save(CreateEntitiesUtils.mockRootCategory())
        val childCategory = categoryRepository.save(CreateEntitiesUtils.mockCategoryWithParent(rootCategory))
        val child1Category = categoryRepository.save(CreateEntitiesUtils.mockCategoryWithParent(rootCategory))
        val childChildCategory = categoryRepository.save(CreateEntitiesUtils.mockCategoryWithParent(childCategory))

        val result = categoryRepository.getTreeById(rootCategory.id)

        result shouldContainExactlyInAnyOrder listOf(rootCategory, childCategory, child1Category, childChildCategory)
    }

    @Test
    fun `If category dont have family, return only it`() {
        val rootCategory = categoryRepository.save(CreateEntitiesUtils.mockRootCategory())

        val result = categoryRepository.getTreeById(rootCategory.id)

        result shouldBe listOf(rootCategory)
    }
}
