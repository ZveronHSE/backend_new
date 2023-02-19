package ru.zveron.service

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import ru.zveron.contract.parameter.internal.ParameterValueRequest
import ru.zveron.entity.Parameter
import ru.zveron.entity.ParameterFromType
import ru.zveron.exception.ParameterException
import ru.zveron.model.ParameterType
import ru.zveron.repository.LotFormRepository
import ru.zveron.repository.ParameterFromTypeRepository
import ru.zveron.util.ValidateUtils.validatePositive
import java.time.Instant
import java.time.format.DateTimeParseException

@Service
class ParameterService(
    private val parameterFromTypeRepository: ParameterFromTypeRepository,
    private val categoryService: CategoryService,
    private val lotFormRepository: LotFormRepository
) {
    suspend fun getAllParameters(categoryId: Int, lotFormId: Int): List<ParameterFromType> {
        categoryId.validatePositive("categoryId")
        lotFormId.validatePositive("lotFormId")

        return coroutineScope {
            val coroutineCategory = async {
                categoryService.getChildOfRootAncestor(categoryId)
            }
            val coroutineLotForm = async {
                lotFormRepository.getLotFormByIdOrThrow(lotFormId)
            }

            parameterFromTypeRepository.getAllByCategoryAndLotForm(coroutineCategory.await(), coroutineLotForm.await())
        }
    }

    suspend fun validateValuesForParameters(request: ParameterValueRequest) {
        val parameters = getAllParameters(request.categoryId, request.lotFormId)
            .associate { it.parameter.id to it.parameter }

        val sourceParameters = request.parameterValuesMap.toMutableMap()
        for ((id, parameter) in parameters) {
            val parameterValue = sourceParameters[id]

            // Случай, если пользователь не заполнял значение.
            if (parameterValue == null) {
                // Но надо сделать атата, если не заполнили, а он то требуется :)
                if (parameter.isRequired) {
                    throw ParameterException("Для параметра '${parameter.name}', id=${id} обязательное значение отсутствует")
                }

                continue
            }

            val success = when (parameter.type) {
                ParameterType.STRING.type -> parameter.checkValueInValues(parameterValue)
                ParameterType.INTEGER.type -> parameter.validateIntegerValueForParameter(parameterValue)
                ParameterType.DATE.type -> parameter.validateDateValueForParameter(parameterValue)
                else -> false
            }

            if (!success) {
                throw ParameterException("Для параметра '${parameter.name}', id=${id} значение $parameterValue неподходящее")
            }

            // Удаляем после всей обработки
            sourceParameters.remove(id)
        }

        // Проверяем, что нет никаких лишних параметров.
        sourceParameters.forEach { (id, _) ->
            if (parameters[id] == null) throw ParameterException("Был передан неизвестный параметр id=${id}")
        }
    }


    private fun Parameter.validateIntegerValueForParameter(valueParameter: String): Boolean {
        valueParameter.toIntOrNull()
            ?: throw ParameterException("Для параметра '${name}', id=${id} значение $valueParameter не является числом")


        return checkValueInValues(valueParameter)
    }

    private fun Parameter.validateDateValueForParameter(valueParameter: String): Boolean {
        try {
            Instant.parse(valueParameter)
        } catch (e: DateTimeParseException) {
            throw ParameterException("Для параметра '${name}', id=${id} значение $valueParameter не является датой")
        }

        return checkValueInValues(valueParameter)
    }

    /**
     * Проверяет, что значение есть в списке значений
     */
    private fun Parameter.checkValueInValues(value: String): Boolean {
        if (list_value != null) {
            return list_value.any { it == value }
        }

        return true
    }
}
