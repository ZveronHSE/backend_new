package ru.zveron.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.zveron.entity.Category
import ru.zveron.repository.CategoryRepository

@Service
class CategoryService(val categoryRepository: CategoryRepository) {
    @Transactional
    fun getChildByCategory(categoryID: Long): List<Category> {
        val categoryParent = getCategoryByIDOrThrow(categoryID)

        return categoryParent.categories.toList()
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
    fun getChildOfRootAncestor(categoryID: Long): Category {
        var category = getCategoryByIDOrThrow(categoryID)

        // Если подали на вход корня всех категорий(животные или товары для животных) кинем исключение)0)
        if (category.parent == null) {
//            throw CategoryException("Категория является корневой, она не может быть ребенком по определению.")
        }

        // Пример: Собака -> category.parent = Животные
        // Животные?.parent = null, но так как у нас тут ? - то в целом уже на Животном прекращается выполнение и вернем
        while (category.parent?.parent != null) {
            category = category.parent!!
        }

        return category
    }

    /**
     * Возвращает корневую категорию для [category]
     * Важно: метод должен вызываться внутри активной транзакции
     */
    fun getRoot(category: Category): Category {
        var lastCategory = category
        while (lastCategory.parent != null) {
            lastCategory = lastCategory.parent!!
        }
        return lastCategory
    }

    fun getCategoryByIDOrThrow(categoryID: Long): Category = categoryRepository.findById(categoryID)
        .orElseThrow { Exception("Категории с id=$categoryID не существует") }
}