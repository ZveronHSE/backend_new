package ru.zveron.service.api

import mu.KLogging
import net.devh.boot.grpc.server.service.GrpcService
import net.logstash.logback.marker.Markers.append
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

    companion object : KLogging()

    override suspend fun getAnimal(request: GetAnimalRequestInt): GetAnimalResponseInt {
        request.validate()

        logger.debug(append("request", request)) { "Request received" }
        return animalService.getAnimal(request.animalId).let { GetAnimalResponseIntKt.ofEntity(it) }
    }

    override suspend fun getAnimalBatch(request: GetAnimalBatchRequest): GetAnimalBatchResponse {
        request.validate()

        return animalService.getAnimalBatch(request)
    }
}
