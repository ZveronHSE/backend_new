package ru.zveron.util

import ru.zveron.contract.category.category
import ru.zveron.contract.category.categoryRequest
import ru.zveron.contract.category.categoryResponse
import ru.zveron.entity.Category

object CreateEntitiesUtil {
    fun mockCategoryWithParent(category: Category) = Category(
        name = "child",
        parent = category
    )


    fun mockRootCategory() = Category(
        name = "root"
    )

    fun mockCategoryRequest(id: Int) = categoryRequest {
        this.id = id
    }

    private fun mapFromCategoryToContract(category: Category) = category {
        id = category.id
        name = category.name
    }

    fun mapCategoriesToResponse(vararg categories: Category) = categoryResponse {
        this.categories.addAll(categories.map { mapFromCategoryToContract(it) })
    }
}