package ru.zveron.service.api

import com.google.protobuf.Empty
import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.contract.profile.AnimalGrpcExternalGrpcKt
import ru.zveron.contract.profile.CreateAnimalRequest
import ru.zveron.contract.profile.CreateAnimalResponse
import ru.zveron.contract.profile.GetAnimalRequestExt
import ru.zveron.contract.profile.GetAnimalResponseExt
import ru.zveron.contract.profile.GetAnimalResponseExtKt
import ru.zveron.contract.profile.GetAnimalsByProfileResponse
import ru.zveron.library.grpc.util.GrpcUtils
import ru.zveron.mapper.AnimalMapper.ofEntity
import ru.zveron.service.AnimalService
import ru.zveron.validation.AnimalContractValidator.validate
import kotlin.coroutines.coroutineContext

@GrpcService
class AnimalServiceExternal(
    private val animalService: AnimalService,
) : AnimalGrpcExternalGrpcKt.AnimalGrpcExternalCoroutineImplBase() {


    override suspend fun getAnimal(request: GetAnimalRequestExt): GetAnimalResponseExt {
        request.validate()

        return animalService.getAnimal(request.animalId).let { GetAnimalResponseExtKt.ofEntity(it) }
    }

    override suspend fun getAnimalsByProfile(request: Empty): GetAnimalsByProfileResponse {
        val profileId = GrpcUtils.getMetadata(coroutineContext).profileId!!

        return animalService.getAnimalsByProfile(profileId)
    }

    override suspend fun createAnimal(request: CreateAnimalRequest): CreateAnimalResponse {
        request.validate()

        val profileId = GrpcUtils.getMetadata(coroutineContext).profileId!!

        return animalService.createAnimal(request, profileId)
    }
}
