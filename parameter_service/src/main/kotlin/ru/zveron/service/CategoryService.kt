package ru.zveron.service

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.zveron.entity.Category
import ru.zveron.repository.CategoryRepository
import ru.zveron.util.ValidateUtils.validatePositive

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository
) {
    fun getChildren(id: Int): List<Category> {
        id.validatePositive("categoryId")

        return categoryRepository.getCategoriesByParent_IdEquals(id)
    }

    @Cacheable("rootCategories")
    fun getRootCategories(): List<Category> {
        return categoryRepository.getCategoriesByParentIsNull()
    }

    fun getCategoryByIDOrThrow(id: Int) = categoryRepository.getCategoryByIDOrThrow(id)


    fun getTree(id: Int): List<Category> {
        id.validatePositive("categoryId")

        val categoryParent = categoryRepository.getCategoryByIDOrThrow(id)

        return categoryRepository.getTreeById(categoryParent.id)
    }

    /**
     * Мы ищем рутовую категорию для конкретной категории
     *
     * Если на вход мы передали рутовую категорию, то мы сразу же возвращаем категорию.
     * Если на вход мы передали потомка, то мы ищем родителя до тех пор, пока не найдем рутовую категорию
     */
    @Transactional
    fun getRootCategoryByChild(category: Category): Category {
        // Если подали на вход корня всех категорий(животные или товары для животных) кинем исключение)0)
        if (category.parent == null) {
            return category
        }

        var newCategory = category
        // Идем циклом вверх, пока не дойдем до рутовой категории
        while (newCategory.parent != null) {
            newCategory = newCategory.parent!!
        }

        return newCategory
    }

}