package ru.zveron.service

import org.springframework.stereotype.Service
import ru.zveron.contract.profile.CreateAnimalRequest
import ru.zveron.contract.profile.CreateAnimalResponse
import ru.zveron.contract.profile.CreateAnimalResponseKt
import ru.zveron.contract.profile.GetAnimalBatchRequest
import ru.zveron.contract.profile.GetAnimalBatchResponse
import ru.zveron.contract.profile.GetAnimalsByProfileResponse
import ru.zveron.contract.profile.ListedAnimalKt
import ru.zveron.contract.profile.getAnimalBatchResponse
import ru.zveron.contract.profile.getAnimalsByProfileResponse
import ru.zveron.contract.profile.model.FullAnimalKt
import ru.zveron.entity.Animal
import ru.zveron.exception.AnimalNotFoundException
import ru.zveron.mapper.AnimalMapper.of
import ru.zveron.mapper.AnimalMapper.ofEntity
import ru.zveron.mapper.AnimalMapper.toEntity
import ru.zveron.repository.AnimalRepository
import ru.zveron.repository.ProfileRepository

@Service
class AnimalService(
    private val animalRepository: AnimalRepository,
    private val profileRepository: ProfileRepository,
) {

    suspend fun createAnimal(request: CreateAnimalRequest, profileId: Long): CreateAnimalResponse {
        val profile =
            profileRepository.findById(profileId).orElseThrow { IllegalArgumentException("Profile not found") }

        val animal = animalRepository.save(request.toEntity(profile))

        return CreateAnimalResponseKt.ofEntity(animal)
    }

    suspend fun getAnimal(animalId: Long): Animal {
        return animalRepository.findById(animalId)
            .orElseThrow { AnimalNotFoundException("Animal not found $animalId") }
    }

    suspend fun getAnimalsByProfile(profileId: Long): GetAnimalsByProfileResponse =
        animalRepository.findAllByProfileId(profileId)
            .map { ListedAnimalKt.ofEntity(it) }
            .let {
                getAnimalsByProfileResponse { this.animals.addAll(it) }
            }

    suspend fun getAnimalBatch(request: GetAnimalBatchRequest): GetAnimalBatchResponse {
        return animalRepository.findAllById(request.animalIdsList)
            .map { FullAnimalKt.of(it) }
            .let { getAnimalBatchResponse { this.animals.addAll(it) } }
    }
}
