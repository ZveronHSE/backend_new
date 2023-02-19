package ru.zveron.grpc

import com.google.protobuf.BoolValue
import com.google.protobuf.Int32Value
import com.google.protobuf.boolValue
import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.contract.parameter.internal.CategoryServiceGrpcKt
import ru.zveron.contract.parameter.internal.CategoryTreeResponse
import ru.zveron.contract.parameter.internal.category
import ru.zveron.contract.parameter.internal.categoryTreeResponse
import ru.zveron.service.CategoryService

@GrpcService
class CategoryInternalController(
    private val categoryService: CategoryService
) : CategoryServiceGrpcKt.CategoryServiceCoroutineImplBase() {
    override suspend fun categoryHasChildren(request: Int32Value): BoolValue {
        val categories = categoryService.getChildren(request.value)

        return boolValue {
            value = categories.isNotEmpty()
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