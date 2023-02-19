package ru.zveron.client.parameter

import com.google.protobuf.int32Value
import io.grpc.Status
import io.grpc.StatusException
import org.springframework.stereotype.Service
import ru.zveron.contract.parameter.internal.CategoryServiceGrpcKt
import ru.zveron.contract.parameter.internal.InfoCategory
import ru.zveron.contract.parameter.internal.ParameterServiceGrpcKt
import ru.zveron.contract.parameter.internal.parameterValueRequest
import ru.zveron.contract.parameter.internal.parametersRequest
import ru.zveron.exception.LotException
import ru.zveron.util.ValidateUtils.validatePositive

@Service
class ParameterClient(
    private val parameterStub: ParameterServiceGrpcKt.ParameterServiceCoroutineStub,
    private val categoryStub: CategoryServiceGrpcKt.CategoryServiceCoroutineStub
) {
    suspend fun getTreeByCategory(categoryId: Int): List<Int> {
        categoryId.validatePositive("categoryId")

        val request = int32Value {
            value = categoryId
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

    suspend fun getInfoAboutCategory(categoryId: Int): InfoCategory {
        categoryId.validatePositive("categoryId")

        val request = int32Value {
            value = categoryId
        }

        return try {
            categoryStub.getInfoAboutCategory(request)
        } catch (ex: StatusException) {
            throw LotException(
                Status.INTERNAL,
                "Can't get info by category id for categoryId=$categoryId. Status: ${ex.status.description}"
            )
        }
    }

    suspend fun getParametersById(parametersId: List<Int>): Map<Int, String> {
        val request = parametersRequest {
            parameterIds.addAll(parametersId)
        }

        return try {
            val response = parameterStub.getParametersByIds(request)

            response.parametersList.associate { it.id to it.name }
        } catch (ex: StatusException) {
            throw LotException(
                Status.INTERNAL,
                "Can't get parameters by ids: $parametersId. Status: ${ex.status.description}"
            )
        }
    }
}
