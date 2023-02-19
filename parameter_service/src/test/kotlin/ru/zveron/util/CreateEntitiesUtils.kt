package ru.zveron.util

import com.google.protobuf.int32Value
import ru.zveron.contract.parameter.external.categoryResponse
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


    fun mockIntWrapper(id: Int) = int32Value {
        value = id
    }

    fun mapToInternalCategory(category: Category) = ru.zveron.contract.parameter.internal.category {
        id = category.id
        name = category.name
    }

    fun mapCategoriesToExternalResponse(vararg categories: Category) = categoryResponse {
        this.categories.addAll(categories.map { mapToExternalCategory(it) })
    }

    private fun mapToExternalCategory(category: Category) = ru.zveron.contract.parameter.external.category {
        id = category.id
        name = category.name
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
        LotForm(id, Category(id, ""), ""),
        Parameter(
            id = id,
            name = name,
            type = type,
            isRequired = isRequired,
            list_value = listValue
        )
    )
}