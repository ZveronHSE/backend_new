package ru.zveron.service

import io.grpc.Status
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.zveron.entity.Category
import ru.zveron.exception.CategoryException
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


    fun getTree(id: Int): List<Category> {
        id.validatePositive("categoryId")

        val categoryParent = categoryRepository.getCategoryByIDOrThrow(id)

        return categoryRepository.getTreeById(categoryParent.id)
    }

    /**
     * Мы ищем категорию, которая будет являться ребенком рутовых категорий. Например: Животные->Собака - в нашем случае
     * это Собака.
     *
     * Если на вход мы передали такого ребенка, то мы сразу же возвращаем категорию.
     * Если на вход мы передали потомка, то мы ищем родителя до тех пор, пока не найдем нужного ребенка.
     * Если на вход подали корней, то штош F
     */
    @Transactional
    fun getChildOfRootAncestor(categoryId: Int): Category {
        var category = categoryRepository.getCategoryByIDOrThrow(categoryId)

        // Если подали на вход корня всех категорий(животные или товары для животных) кинем исключение)0)
        if (category.parent == null) {
            throw CategoryException(
                Status.INVALID_ARGUMENT,
                "Категория является корневой, она не может быть ребенком по определению"
            )
        }

        // Пример: Собака -> category.parent = Животные
        // Животные?.parent = null, но так как у нас тут ? - то в целом уже на Животном прекращается выполнение и вернем
        while (category.parent?.parent != null) {
            category = category.parent!!
        }

        return category
    }

}