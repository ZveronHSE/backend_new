package ru.zveron.mapper

import ru.zveron.contract.parameter.external.CategoryResponse
import ru.zveron.contract.parameter.external.category
import ru.zveron.contract.parameter.external.categoryResponse
import ru.zveron.entity.Category

object CategoryMapper {

    fun List<Category>.toCategoryResponse(): CategoryResponse {
        val categories = map {
            category {
                id = it.id
                name = it.name
            }
        }

        return categoryResponse { this.categories.addAll(categories) }
    }
}