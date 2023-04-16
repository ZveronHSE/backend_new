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
        require(age > 0) { "Animal must have a positive age" }
        require(breed.isNotBlank()) { "Animal must have a breed" }
        require(name.isNotBlank()) { "Animal must have a name" }
        require(this.imageUrlsList.isNotEmpty()) { "Animal must have at least 1 image" }
    }

    fun GetAnimalRequestInt.validate() = require(this.animalId > 0) { "Animal entity must have a positive id" }

    fun GetAnimalBatchRequest.validate() = require(animalIdsList.none { it <= 0 }) { "Animal ids must all be positive" }
}
