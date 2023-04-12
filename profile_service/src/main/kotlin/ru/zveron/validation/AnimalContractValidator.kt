package ru.zveron.validation

import ru.zveron.contract.profile.CreateAnimalRequest
import ru.zveron.contract.profile.GetAnimalBatchRequest
import ru.zveron.contract.profile.GetAnimalRequestExt
import ru.zveron.contract.profile.GetAnimalRequestInt

@Suppress("unused")
object AnimalContractValidator {
    fun GetAnimalRequestExt.validate() =
        this.animalId.takeIf { it > 0 } ?: throw IllegalArgumentException("Animal id must be greater than 0")

    fun CreateAnimalRequest.validate() = with(this) {
        this.age.takeIf { it > 0 } ?: throw IllegalArgumentException("Animal age must be greater than 0")
        this.breed.takeIf { it.isNotBlank() } ?: throw IllegalArgumentException("Animal breed must be not blank")
        this.name.takeIf { it.isNotBlank() } ?: throw IllegalArgumentException("Animal name must be not blank")
    }

    fun GetAnimalRequestInt.validate() = with(this) {
        this.animalId.takeIf { it > 0 } ?: throw IllegalArgumentException("Animal id must be greater than 0")
    }

    fun GetAnimalBatchRequest.validate() = with(this) {
        this.animalIdsList.takeUnless { it.any { id -> id < 1 } }
            ?: throw IllegalArgumentException("Animal ids must be not empty")
    }
}