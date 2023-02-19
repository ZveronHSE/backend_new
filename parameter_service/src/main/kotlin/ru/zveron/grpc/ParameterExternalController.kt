package ru.zveron.grpc

import com.google.protobuf.Int32Value
import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.contract.parameter.external.LotFormResponse
import ru.zveron.contract.parameter.external.ParameterExternalServiceGrpcKt
import ru.zveron.contract.parameter.external.ParameterRequest
import ru.zveron.contract.parameter.external.ParameterResponse
import ru.zveron.contract.parameter.external.lotForm
import ru.zveron.contract.parameter.external.lotFormResponse
import ru.zveron.mapper.ParameterMapper.toResponse
import ru.zveron.service.LotFormService
import ru.zveron.service.ParameterService

@GrpcService
class ParameterExternalController(
    private val parameterService: ParameterService,
    private val lotFormService: LotFormService
) : ParameterExternalServiceGrpcKt.ParameterExternalServiceCoroutineImplBase() {
    override suspend fun getLotForms(request: Int32Value): LotFormResponse {
        val lotForms = lotFormService.getLotFormsByCategoryId(request.value)

        val lotFormsDto = lotForms.map {
            lotForm {
                id = it.id
                name = it.type
            }
        }

        return lotFormResponse {
            this.lotForms.addAll(lotFormsDto)
        }
    }

    override suspend fun getParameters(request: ParameterRequest): ParameterResponse {
        val parameters = parameterService.getAllParameters(request.categoryId, request.lotFormId)

        return parameters.toResponse()
    }

}