package ru.zveron.mapper

import ru.zveron.contract.parameter.external.ParameterResponse
import ru.zveron.contract.parameter.external.Type
import ru.zveron.contract.parameter.external.parameter
import ru.zveron.contract.parameter.external.parameterResponse
import ru.zveron.entity.ParameterFromType

object ParameterMapper {
    fun List<ParameterFromType>.toResponse(): ParameterResponse {
        val parameters = this.map {
            val parameter = it.parameter

            parameter {
                id = parameter.id
                name = parameter.name
                type = Type.valueOf(parameter.type)
                isRequired = parameter.isRequired
                parameter.list_value?.let { listValue -> values.addAll(listValue) }
            }
        }

        return parameterResponse {
            this.parameters.addAll(parameters)
        }
    }
}