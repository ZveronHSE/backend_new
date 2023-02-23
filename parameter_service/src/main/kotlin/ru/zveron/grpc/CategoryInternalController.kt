package ru.zveron.grpc

import com.google.protobuf.Int32Value
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import mu.KLogging
import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.contract.parameter.internal.CategoryServiceGrpcKt
import ru.zveron.contract.parameter.internal.CategoryTreeResponse
import ru.zveron.contract.parameter.internal.InfoCategory
import ru.zveron.contract.parameter.internal.category
import ru.zveron.contract.parameter.internal.categoryTreeResponse
import ru.zveron.contract.parameter.internal.infoCategory
import ru.zveron.entity.Category
import ru.zveron.service.CategoryService

@GrpcService
class CategoryInternalController(
    private val categoryService: CategoryService
) : CategoryServiceGrpcKt.CategoryServiceCoroutineImplBase() {

    companion object : KLogging() {
        const val CATEGORY_ANIMAL_ID = 1
    }

    override suspend fun getInfoAboutCategory(request: Int32Value): InfoCategory {
        val category: Category = categoryService.getCategoryByIDOrThrow(request.value)

        var children: List<Category> = listOf()
        var childOfRoot: Category? = null

        coroutineScope {
            val clients = mutableListOf(
                async {
                    children = categoryService.getChildren(category.id)
                },
                async {
                    childOfRoot = categoryService.getRootCategoryByChild(category)
                }
            )

            return@coroutineScope clients.awaitAll()
        }


        return infoCategory {
            this.category = category {
                id = category.id
                name = category.name
            }

            hasGender = childOfRoot?.id == CATEGORY_ANIMAL_ID
            hasChildren = children.isNotEmpty()
        }
    }


    override suspend fun getCategoryTree(request: Int32Value): CategoryTreeResponse {
        val categories = categoryService.getTree(request.value).map {
            category {
                id = it.id
                name = it.name
            }
        }

        return categoryTreeResponse { this.categories.addAll(categories) }
    }
}