package ru.zveron.mapper

import ru.zveron.contract.parameter.model.ParameterResponse
import ru.zveron.contract.parameter.model.Type
import ru.zveron.contract.parameter.model.parameter
import ru.zveron.contract.parameter.model.parameterResponse
import ru.zveron.entity.Parameter
import ru.zveron.entity.ParameterFromType

object ParameterMapper {
    fun List<ParameterFromType>.toResponse(): ParameterResponse {
        val parameters = this.map { it.parameter.toContract() }

        return parameterResponse {
            this.parameters.addAll(parameters)
        }
    }

    fun List<Parameter>.toParameterResponse(): ParameterResponse {
        val parameters = this.map { it.toContract() }

        return parameterResponse {
            this.parameters.addAll(parameters)
        }
    }

    private fun Parameter.toContract(): ru.zveron.contract.parameter.model.Parameter {
        val parameter = this

        return parameter {
            id = parameter.id
            name = parameter.name
            type = Type.valueOf(parameter.type)
            isRequired = parameter.isRequired
            parameter.list_value?.let { listValue -> values.addAll(listValue) }
        }
    }
}