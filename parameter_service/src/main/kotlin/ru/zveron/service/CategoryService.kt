package ru.zveron.service

import io.grpc.Status
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.transaction.annotation.Transactional
import ru.zveron.contract.category.CategoryRequest
import ru.zveron.contract.category.CategoryResponse
import ru.zveron.contract.category.CategoryServiceGrpcKt
import ru.zveron.contract.category.category
import ru.zveron.contract.category.categoryResponse
import ru.zveron.entity.Category
import ru.zveron.exception.CategoryException
import ru.zveron.repository.CategoryRepository

@GrpcService
class CategoryService(
    private val categoryRepository: CategoryRepository
) : CategoryServiceGrpcKt.CategoryServiceCoroutineImplBase() {
    // TODO подключить к контракту ApiGateway /api/categories/{id}
    @Transactional
    override suspend fun getChild(request: CategoryRequest): CategoryResponse {
        val categoryParent = categoryRepository.getCategoryByIDOrThrow(request.id)

        val categories = categoryParent.subCategories.map {
            category {
                id = it.id
                name = it.name
            }
        }

        return categoryResponse { this.categories.addAll(categories) }
    }

    override suspend fun getCategoryTree(request: CategoryRequest): CategoryResponse {
        val category = categoryRepository.getCategoryByIDOrThrow(request.id)

        val categories = categoryRepository.getTreeById(category.id).map {
            category {
                id = it.id
                name = it.name
            }
        }

        return categoryResponse { this.categories.addAll(categories) }
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
