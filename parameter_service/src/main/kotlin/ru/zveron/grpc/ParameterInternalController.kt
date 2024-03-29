package ru.zveron.grpc

import com.google.protobuf.Empty
import com.google.protobuf.empty
import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.contract.parameter.internal.ParameterServiceGrpcKt
import ru.zveron.contract.parameter.internal.ParameterValueRequest
import ru.zveron.contract.parameter.internal.ParametersRequest
import ru.zveron.contract.parameter.model.ParameterResponse
import ru.zveron.mapper.ParameterMapper.toParameterResponse
import ru.zveron.service.ParameterService

@GrpcService
class ParameterInternalController(
    private val parameterService: ParameterService
) : ParameterServiceGrpcKt.ParameterServiceCoroutineImplBase() {
    override suspend fun validateValuesForParameters(request: ParameterValueRequest): Empty {
        parameterService.validateValuesForParameters(request)

        return empty {}
    }

    override suspend fun getParametersByIds(request: ParametersRequest): ParameterResponse {
        val parameters = parameterService.getAllParametersById(request.parameterIdsList)

        return parameters.toParameterResponse()
    }
}
