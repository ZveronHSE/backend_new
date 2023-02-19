package ru.zveron.grpc

import com.google.protobuf.Empty
import com.google.protobuf.Int32Value
import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.contract.parameter.external.CategoryExternalServiceGrpcKt
import ru.zveron.contract.parameter.external.CategoryResponse
import ru.zveron.mapper.CategoryMapper.toCategoryResponse
import ru.zveron.service.CategoryService

@GrpcService
class CategoryExternalController(
    private val categoryService: CategoryService
) : CategoryExternalServiceGrpcKt.CategoryExternalServiceCoroutineImplBase() {

    override suspend fun getChildren(request: Int32Value): CategoryResponse {
        val subCategories = categoryService.getChildren(request.value)

        return subCategories.toCategoryResponse()
    }

    override suspend fun getRoot(request: Empty): CategoryResponse {
        val categories = categoryService.getRootCategories()

        return categories.toCategoryResponse()
    }
}