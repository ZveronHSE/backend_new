package ru.zveron.client.parameter

import io.grpc.Status
import io.grpc.StatusException
import org.springframework.stereotype.Service
import ru.zveron.contract.category.CategoryServiceGrpcKt
import ru.zveron.contract.category.categoryRequest
import ru.zveron.contract.parameter.ParameterServiceGrpcKt
import ru.zveron.contract.parameter.parameterValueRequest
import ru.zveron.exception.LotException
import ru.zveron.util.ValidateUtils.validatePositive

@Service
class ParameterClient(
    private val parameterStub: ParameterServiceGrpcKt.ParameterServiceCoroutineStub,
    private val categoryStub: CategoryServiceGrpcKt.CategoryServiceCoroutineStub
) {
    suspend fun getTreeByCategory(categoryId: Int): List<Int> {
        categoryId.validatePositive("categoryId")

        val request = categoryRequest {
            id = categoryId
        }

        return try {
            val response = categoryStub.getCategoryTree(request)

            response.categoriesList.map { it.id }
        } catch (ex: StatusException) {
            throw LotException(
                Status.INTERNAL,
                "Can't get tree by category for categoryId=$categoryId. Status: ${ex.status.description}"
            )
        }
    }

    suspend fun validateParameters(categoryId: Int, lotFormId: Int, parameters: Map<Int, String>) {
        categoryId.validatePositive("categoryId")
        lotFormId.validatePositive("lotFormId")


        val request = parameterValueRequest {
            this.categoryId = categoryId
            this.lotFormId = lotFormId
            parameterValues.putAll(parameters)
        }

        try {
            parameterStub.validateValuesForParameters(request)
        } catch (ex: StatusException) {
            if (ex.status.code == Status.INVALID_ARGUMENT.code) {
                throw LotException(Status.INVALID_ARGUMENT, "${ex.status.description}")
            } else {
                throw LotException(
                    Status.INTERNAL,
                    "Can't get answer validate for categoryId=$categoryId and lotFormId=$lotFormId. Status: ${ex.status.description}"
                )
            }
        }
    }
}
