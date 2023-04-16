package ru.zveron.service.api

import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.contract.profile.AnimalServiceInternalGrpcKt
import ru.zveron.contract.profile.GetAnimalBatchRequest
import ru.zveron.contract.profile.GetAnimalBatchResponse
import ru.zveron.contract.profile.GetAnimalRequestInt
import ru.zveron.contract.profile.GetAnimalResponseInt
import ru.zveron.contract.profile.GetAnimalResponseIntKt
import ru.zveron.mapper.AnimalMapper.ofEntity
import ru.zveron.service.AnimalService
import ru.zveron.validation.AnimalContractValidator.validate

@GrpcService
class AnimalServiceInternal(
    private val animalService: AnimalService,
) : AnimalServiceInternalGrpcKt.AnimalServiceInternalCoroutineImplBase() {

    override suspend fun getAnimal(request: GetAnimalRequestInt): GetAnimalResponseInt {
        request.validate()

        return animalService.getAnimal(request.animalId).let { GetAnimalResponseIntKt.ofEntity(it) }
    }

    override suspend fun getAnimalBatch(request: GetAnimalBatchRequest): GetAnimalBatchResponse {
        request.validate()

        return animalService.getAnimalBatch(request)
    }
}
