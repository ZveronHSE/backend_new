package ru.zveron.mapper

import ru.zveron.contract.profile.CreateAnimalRequest
import ru.zveron.contract.profile.CreateAnimalResponseKt
import ru.zveron.contract.profile.GetAnimalResponseExtKt
import ru.zveron.contract.profile.GetAnimalResponseIntKt
import ru.zveron.contract.profile.ListedAnimalKt
import ru.zveron.contract.profile.createAnimalResponse
import ru.zveron.contract.profile.getAnimalResponseExt
import ru.zveron.contract.profile.getAnimalResponseInt
import ru.zveron.contract.profile.listedAnimal
import ru.zveron.contract.profile.model.FullAnimalKt
import ru.zveron.contract.profile.model.fullAnimal
import ru.zveron.entity.Animal
import ru.zveron.entity.Profile

@Suppress("unused")
object AnimalMapper {

    fun CreateAnimalRequest.toEntity(profile: Profile) = Animal(
        name = this.name,
        breed = this.breed,
        age = this.age,
        profile = profile,
        species = this.species,
        imageUrls = this.imageUrlsList.asByteStringList().map { it.toStringUtf8() }.toTypedArray(),
        documentUrls = this.documentUrlsList.asByteStringList().map { it.toStringUtf8() }.toTypedArray(),
    )

    fun CreateAnimalResponseKt.ofEntity(a: Animal) = createAnimalResponse {
        this.animal = FullAnimalKt.of(a)
    }

    fun GetAnimalResponseExtKt.ofEntity(a: Animal) = getAnimalResponseExt {
        this.animal = FullAnimalKt.of(a)
    }

    fun ListedAnimalKt.ofEntity(a: Animal) = listedAnimal {
        this.id = a.id
        this.name = a.name
        this.breed = a.breed
        this.age = a.age
        this.species = a.species
        this.imageUrl = a.imageUrls.firstOrNull() ?: error("Animal must have at least one image")
    }

    fun GetAnimalResponseIntKt.ofEntity(a: Animal) = getAnimalResponseInt {
        this.animal = FullAnimalKt.of(a)
    }

    fun FullAnimalKt.of(a: Animal) = fullAnimal {
        this.id = a.id
        this.name = a.name
        this.breed = a.breed
        this.age = a.age
        this.species = a.species
        this.imageUrls.addAll(a.imageUrls.toList())
        this.documentUrls.addAll(a.documentUrls.toList())
    }
}
