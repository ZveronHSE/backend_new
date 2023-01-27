package ru.zveron.util

import ru.zveron.contract.category.category
import ru.zveron.contract.category.categoryRequest
import ru.zveron.contract.category.categoryResponse
import ru.zveron.contract.parameter.parameterRequest
import ru.zveron.entity.Category
import ru.zveron.entity.LotForm
import ru.zveron.entity.Parameter
import ru.zveron.entity.ParameterFromType

object CreateEntitiesUtils {
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

    fun mockParameterRequest(categoryId: Int, lotFormId: Int) = parameterRequest {
        this.categoryId = categoryId
        this.lotFormId = lotFormId
    }

    fun mockParameterFromType(
        id: Int,
        name: String,
        isRequired: Boolean,
        listValue: List<String>,
        type: String
    ) = ParameterFromType(
        ParameterFromType.ParameterFromTypeKey(id, id, id),
        Category(id, ""),
        LotForm(id, "", ""),
        Parameter(
            id = id,
            name = name,
            type = type,
            isRequired = isRequired,
            list_value = listValue
        )
    )
}
